package com.github.tartaricacid.touhoulittlemaid.ai;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidChat;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidTts;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Java 8/OpenAI-compatible server client with bounded memory and rate limits. */
public final class LegacyMaidChatService {
    private static final Map<UUID,Long> LAST_USE=new ConcurrentHashMap<UUID,Long>();
    private static final ExecutorService EXECUTOR=Executors.newFixedThreadPool(2,new java.util.concurrent.ThreadFactory(){public Thread newThread(Runnable r){Thread t=new Thread(r,"TLM-AI-Chat");t.setDaemon(true);return t;}});
    private LegacyMaidChatService(){}

    public static String validate(EntityPlayerMP player,EntityMaid maid,String message){
        if(!LegacyConfig.enableMaidChat||LegacyConfig.chatServiceUrl.trim().isEmpty())return "AI chat is disabled in the server config";
        if(maid==null||maid.getOwner()!=player||maid.getDistanceSqToEntity(player)>256)return "No owned maid within 16 blocks";
        if(message==null||message.trim().isEmpty()||message.length()>256)return "Message must contain 1-256 characters";
        long now=System.currentTimeMillis(),last=LAST_USE.containsKey(player.getUniqueID())?LAST_USE.get(player.getUniqueID()):0;
        if(now-last<LegacyConfig.chatCooldownSeconds*1000L)return "AI chat cooldown: "+((LegacyConfig.chatCooldownSeconds*1000L-now+last+999)/1000)+"s";
        try{URL url=new URL(LegacyConfig.chatServiceUrl);if(!"http".equals(url.getProtocol())&&!"https".equals(url.getProtocol()))return "Only HTTP(S) AI endpoints are allowed";}catch(Exception e){return "Invalid AI endpoint";}
        LAST_USE.put(player.getUniqueID(),now);return null;
    }

    public static void chat(final EntityPlayerMP player,final EntityMaid maid,final String userText){
        maid.addChatHistory("user",userText);NetworkHandler.channel.sendTo(new MessageMaidChat(maid.getEntityId(),player.getCommandSenderName()+": "+userText,false),player);
        final List<String[]> history=maid.getChatHistory();final String task=maid.getTaskId();final int hunger=maid.getHunger(),favorability=maid.getFavorability();
        EXECUTOR.execute(new Runnable(){public void run(){try{
            final String answer=request(history,task,hunger,favorability);byte[] speech=null;
            if(LegacyConfig.enableTts&&!LegacyConfig.ttsServiceUrl.trim().isEmpty())try{speech=requestTts(answer);}catch(Exception ttsError){TouhouLittleMaid.LOGGER.warn("Maid TTS failed: "+ttsError.getMessage());}
            final byte[] audio=speech;ServerThreadDispatcher.enqueue(new Runnable(){public void run(){if(!valid(player,maid))return;maid.addChatHistory("assistant",answer);applyActions(maid,answer);NetworkHandler.channel.sendTo(new MessageMaidChat(maid.getEntityId(),answer,false),player);if(audio!=null&&audio.length>0)NetworkHandler.channel.sendTo(new MessageMaidTts(maid.getEntityId(),audio),player);else if(LegacyConfig.enableTts)maid.playMaidVoice("maid.mode.idle");}});
        }catch(final Exception e){TouhouLittleMaid.LOGGER.warn("Maid AI chat failed",e);ServerThreadDispatcher.enqueue(new Runnable(){public void run(){if(valid(player,maid))NetworkHandler.channel.sendTo(new MessageMaidChat(maid.getEntityId(),"AI service unavailable: "+safe(e.getMessage()),true),player);}});}}});
    }

    private static boolean valid(EntityPlayerMP player,EntityMaid maid){return player!=null&&maid!=null&&!player.isDead&&!maid.isDead&&player.worldObj==maid.worldObj&&player.worldObj.getEntityByID(maid.getEntityId())==maid&&maid.getOwner()==player;}
    private static String request(List<String[]> history,String task,int hunger,int favorability)throws Exception{
        JsonObject body=new JsonObject();body.addProperty("model",LegacyConfig.chatModel);JsonArray messages=new JsonArray();
        JsonObject system=new JsonObject();system.addProperty("role","system");system.addProperty("content",LegacyPromptLoader.get()+"\nMaid state: task="+task+", hunger="+hunger+", favorability="+favorability);messages.add(system);
        for(String[] turn:history){JsonObject line=new JsonObject();line.addProperty("role",turn[0]);line.addProperty("content",turn[1]);messages.add(line);}body.add("messages",messages);
        HttpURLConnection connection=(HttpURLConnection)new URL(LegacyConfig.chatServiceUrl).openConnection();int timeout=LegacyConfig.chatTimeoutSeconds*1000;connection.setConnectTimeout(timeout);connection.setReadTimeout(timeout);connection.setRequestMethod("POST");connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/json; charset=utf-8");if(!LegacyConfig.chatApiKey.isEmpty())connection.setRequestProperty("Authorization","Bearer "+LegacyConfig.chatApiKey);
        byte[] bytes=body.toString().getBytes("UTF-8");connection.setFixedLengthStreamingMode(bytes.length);OutputStream out=connection.getOutputStream();out.write(bytes);out.close();int status=connection.getResponseCode();InputStream stream=status>=200&&status<300?connection.getInputStream():connection.getErrorStream();String response=read(stream,65536);connection.disconnect();if(status<200||status>=300)throw new java.io.IOException("HTTP "+status+": "+safe(response));
        JsonObject root=new JsonParser().parse(response).getAsJsonObject();String answer="";if(root.has("choices")){JsonArray choices=root.getAsJsonArray("choices");if(choices.size()>0){JsonObject first=choices.get(0).getAsJsonObject();if(first.has("message"))answer=first.getAsJsonObject("message").get("content").getAsString();else if(first.has("text"))answer=first.get("text").getAsString();}}else if(root.has("text"))answer=root.get("text").getAsString();if(answer.trim().isEmpty())throw new java.io.IOException("Empty AI response");return answer.length()>1024?answer.substring(0,1024):answer;
    }
    private static void applyActions(EntityMaid maid,String text){if(text.contains("[sit]"))maid.setMaidSitting(true);if(text.contains("[follow]"))maid.setMaidSitting(false);int start=text.indexOf("[task:");if(start>=0){int end=text.indexOf(']',start);if(end>start){String id=text.substring(start+6,end);if(id.indexOf(':')<0)id="touhou_little_maid:"+id;if(TaskManager.getTasks().containsKey(id))maid.setTaskId(id);}}}
    private static byte[] requestTts(String text)throws Exception{URL url=new URL(LegacyConfig.ttsServiceUrl);if(!"http".equals(url.getProtocol())&&!"https".equals(url.getProtocol()))throw new java.io.IOException("Only HTTP(S) TTS endpoints are allowed");JsonObject body=new JsonObject();body.addProperty("model","tts-1");body.addProperty("voice","alloy");body.addProperty("input",text);body.addProperty("response_format","wav");HttpURLConnection c=(HttpURLConnection)url.openConnection();int timeout=LegacyConfig.chatTimeoutSeconds*1000;c.setConnectTimeout(timeout);c.setReadTimeout(timeout);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");if(!LegacyConfig.chatApiKey.isEmpty())c.setRequestProperty("Authorization","Bearer "+LegacyConfig.chatApiKey);byte[] request=body.toString().getBytes("UTF-8");OutputStream out=c.getOutputStream();out.write(request);out.close();if(c.getResponseCode()<200||c.getResponseCode()>=300)throw new java.io.IOException("TTS HTTP "+c.getResponseCode());InputStream in=c.getInputStream();java.io.ByteArrayOutputStream audio=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int n,total=0;while((n=in.read(buffer))>=0){total+=n;if(total>262144)throw new java.io.IOException("TTS audio exceeds 256 KiB");audio.write(buffer,0,n);}in.close();c.disconnect();return audio.toByteArray();}
    private static String read(InputStream stream,int max)throws Exception{if(stream==null)return "";BufferedReader reader=new BufferedReader(new InputStreamReader(stream,"UTF-8"));StringBuilder out=new StringBuilder();char[] buffer=new char[2048];int n;while((n=reader.read(buffer))>=0){out.append(buffer,0,n);if(out.length()>max)throw new java.io.IOException("AI response exceeds "+max+" characters");}reader.close();return out.toString();}
    private static String safe(String value){if(value==null)return "unknown error";value=value.replace('\n',' ').replace('\r',' ');return value.length()>160?value.substring(0,160):value;}
}
