package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry.Entry;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.*;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.util.*;

/** Resource-pack skin browser using the original selector atlas. */
public final class GuiMaidSkins extends GuiScreen {
    private static final ResourceLocation BG=new ResourceLocation("touhou_little_maid:textures/gui/skin_select.png");
    private static final ResourceLocation SIDE=new ResourceLocation("touhou_little_maid:textures/gui/skin_select_side.png");
    private static String lastPack="",lastSearch="";
    private static int lastPage;
    final EntityMaid maid;
    private final Map<String,MaidSkinPreview> previews=new HashMap<String,MaidSkinPreview>();
    private List<Entry> all,filtered=new ArrayList<Entry>();
    private List<String> packs=new ArrayList<String>();
    private GuiTextField search;
    private int x,y,page,packPage;
    private String pack,query,selected;
    private boolean animate=true;
    private final MaidSkinIconCache icons=new MaidSkinIconCache();
    public GuiMaidSkins(EntityMaid maid){this.maid=maid;pack=lastPack;query=lastSearch;page=lastPage;selected=maid.getModelId();}
    static String tr(String key){return StatCollector.translateToLocal(key);}
    static String label(String text){if(text.startsWith("{")&&text.endsWith("}")){String key=text.substring(1,text.length()-1);String translated=tr(key);return translated.equals(key)?LegacyMaidModelRegistry.INSTANCE.translate(key):translated;}return text;}
    static String name(Entry e){String n=label(e.name);return n.isEmpty()||n.startsWith("model.")?e.id:n;}
    @Override public void initGui(){
        Keyboard.enableRepeatEvents(true);all=LegacyMaidModelRegistry.INSTANCE.getEntries();packs.clear();packs.add("");
        for(Entry e:all)if(!packs.contains(e.packId))packs.add(e.packId);
        if(!packs.contains(pack))pack="";
        x=Math.max(4,Math.min(width-278,width/2-88));y=Math.max(30,(height-180)/2-4);
        search=new GuiTextField(fontRendererObj,x+4,y+183,174,18);search.setMaxStringLength(48);search.setText(query);
        packPage=packs.indexOf(pack)/5;filter(false);
    }
    private void filter(boolean reset){
        if(reset)page=0;filtered.clear();String q=query.toLowerCase(Locale.ROOT).trim();
        for(Entry e:all)if((pack.isEmpty()||pack.equals(e.packId))&&(q.isEmpty()||(name(e)+" "+e.id+" "+label(e.packName)).toLowerCase(Locale.ROOT).contains(q)))filtered.add(e);
        page=Math.max(0,Math.min(page,Math.max(0,(filtered.size()-1)/24)));previews.clear();icons.clear();buttons();
    }
    private void buttons(){
        buttonList.clear();
        buttonList.add(new GuiButton(0,x+184,y+183,34,18,"<"));buttonList.add(new GuiButton(1,x+220,y+183,34,18,">"));
        ((GuiButton)buttonList.get(0)).enabled=page>0;((GuiButton)buttonList.get(1)).enabled=(page+1)*24<filtered.size();
        buttonList.add(new GuiButton(2,4,4,72,20,tr("gui.touhou_little_maid.skin.back")));
        buttonList.add(new GuiButton(3,width-80,4,76,20,tr("gui.touhou_little_maid.skin.details")));
        buttonList.add(new GuiButton(4,80,4,100,20,tr("gui.touhou_little_maid.skin.cache")+": "+(animate?"-":"+")));
        GuiButton up=new GuiButton(7,x+253,y+8,14,18,"^");up.enabled=page>0;buttonList.add(up);
        GuiButton down=new GuiButton(8,x+253,y+151,14,18,"v");down.enabled=(page+1)*24<filtered.size();buttonList.add(down);
        buttonList.add(new GuiButton(5,x-1,y-23,18,20,"<"));buttonList.add(new GuiButton(6,x+252,y-23,18,20,">"));
        for(int i=0;i<5;i++){int index=packPage*5+i;if(index>=packs.size())break;String p=packs.get(index),title=p.isEmpty()?tr("gui.touhou_little_maid.skin.all"):packName(p);
            GuiButton b=new PackButton(10+index,x+19+i*46,y-23,p,title);b.enabled=!p.equals(pack);buttonList.add(b);}
    }
    private final class PackButton extends GuiButton {
        private final String title;private final ResourceLocation icon;
        PackButton(int id,int x,int y,String pack,String title){super(id,x,y,45,20,"");this.title=title;ResourceLocation found=null;for(Entry e:all)if(e.packId.equals(pack)){found=e.packIcon;break;}icon=found;}
        @Override public void drawButton(net.minecraft.client.Minecraft mc,int mx,int my){super.drawButton(mc,mx,my);if(!visible)return;
            if(icon!=null){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(icon);Gui.func_146110_a(xPosition+3,yPosition+4,0,0,12,12,12,12);fontRendererObj.drawString(fontRendererObj.trimStringToWidth(title,26),xPosition+17,yPosition+6,enabled?0xffffff:0xaaaaaa);}
            else drawCenteredString(fontRendererObj,fontRendererObj.trimStringToWidth(title,39),xPosition+22,yPosition+6,enabled?0xffffff:0xaaaaaa);
        }
    }
    private String packName(String id){for(Entry e:all)if(e.packId.equals(id))return label(e.packName);return id;}
    private MaidSkinPreview preview(Entry entry){MaidSkinPreview p=previews.get(entry.id);if(p==null){p=new MaidSkinPreview(maid.worldObj,entry.id);previews.put(entry.id,p);}return p;}
    @Override public void updateScreen(){search.updateCursorCounter();if(!maid.isEntityAlive()||mc.thePlayer.getDistanceSqToEntity(maid)>=64){mc.displayGuiScreen(null);return;}if(animate)for(MaidSkinPreview p:previews.values())p.tickPreview();}
    @Override public void drawScreen(int mx,int my,float partial){
        drawDefaultBackground();GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BG);drawTexturedModalRect(x,y,0,0,256,180);
        mc.getTextureManager().bindTexture(SIDE);drawTexturedModalRect(x+250,y,0,0,24,180);
        int pages=Math.max(1,(filtered.size()+23)/24);drawTexturedModalRect(x+254,y+29+(pages==1?0:page*105/(pages-1)),pages==1?36:24,0,12,15);drawCenteredString(fontRendererObj,(page+1)+" / "+pages,x+125,y+167,0xffffff);
        Entry hovered=null;
        for(int i=0;i<24&&page*24+i<filtered.size();i++){
            Entry e=filtered.get(page*24+i);int cx=x+5+i%6*40,cy=y+5+i/6*39;
            boolean hover=mx>=cx&&mx<cx+39&&my>=cy&&my<cy+38;
            if(animate || !icons.draw(e.id,cx,cy,39,38)){
                MaidSkinPreview.draw(preview(e),cx+19,cy+37,MaidSkinPreview.iconScale(e,19),25,0,partial,cx,cy,39,38);
                if(!animate)icons.capture(e.id,cx,cy,39,38);
            }
            if(e.id.equals(selected)||hover){int color=e.id.equals(selected)?0xff99cc66:0xffaaaaaa;drawRect(cx,cy,cx+39,cy+1,color);drawRect(cx,cy+37,cx+39,cy+38,color);drawRect(cx,cy,cx+1,cy+38,color);drawRect(cx+38,cy,cx+39,cy+38,color);}
            if(e.variant>0)fontRendererObj.drawString("+",cx+31,cy+2,0xffdd88);
            if(hover)hovered=e;
        }
        Entry active=LegacyMaidModelRegistry.INSTANCE.getEntry(selected);
        if(active!=null&&x>45){MaidSkinPreview.draw(preview(active),x/2,y+159,MaidSkinPreview.iconScale(active,48),25,0,partial,0,y,x-4,180);}
        if(filtered.isEmpty())drawCenteredString(fontRendererObj,tr("gui.touhou_little_maid.skin.empty"),x+124,y+80,0xffffff);
        search.drawTextBox();if(query.isEmpty()&&!search.isFocused())fontRendererObj.drawString(tr("gui.touhou_little_maid.skin.search"),x+9,y+188,0x888888);
        super.drawScreen(mx,my,partial);
        if(hovered!=null){List<String> tip=new ArrayList<String>();tip.add(name(hovered));tip.add(hovered.id);if(hovered.variant>0)tip.add(hovered.texture.toString());for(String d:hovered.description)tip.addAll(fontRendererObj.listFormattedStringToWidth(label(d),230));
            if(!hovered.soundPack.isEmpty())tip.add(tr("gui.touhou_little_maid.skin.sound")+": "+hovered.soundPack);
            tip.add(tr("gui.touhou_little_maid.skin.shift"));drawHoveringText(tip,mx,my,fontRendererObj);}
        for(Object o:buttonList){GuiButton b=(GuiButton)o;if(b.id>=10&&mx>=b.xPosition&&mx<b.xPosition+b.width&&my>=b.yPosition&&my<b.yPosition+b.height){String id=packs.get(b.id-10);if(!id.isEmpty()){List<String> tip=new ArrayList<String>();for(Entry e:all)if(e.packId.equals(id)){tip.add(label(e.packName));tip.add(e.author);tip.add(e.version+" / "+e.date);for(String d:e.packDescription)tip.addAll(fontRendererObj.listFormattedStringToWidth(label(d),230));break;}drawHoveringText(tip,mx,my,fontRendererObj);}}}
    }
    @Override protected void actionPerformed(GuiButton b){
        if(b.id==0||b.id==7){page--;filter(false);}else if(b.id==1||b.id==8){page++;filter(false);}else if(b.id==2)back();
        else if(b.id==3){Entry e=LegacyMaidModelRegistry.INSTANCE.getEntry(selected);if(e!=null)mc.displayGuiScreen(new GuiMaidSkinDetails(this,e));}
        else if(b.id==4){animate=!animate;icons.clear();buttons();}else if(b.id==5){packPage=Math.max(0,packPage-1);buttons();}else if(b.id==6){packPage=Math.min((packs.size()-1)/5,packPage+1);buttons();}
        else if(b.id>=10){pack=packs.get(b.id-10);filter(true);}
    }
    @Override protected void mouseClicked(int mx,int my,int button){super.mouseClicked(mx,my,button);search.mouseClicked(mx,my,button);if(button!=0)return;
        if(mx>=x+253&&mx<x+267&&my>=y+29&&my<y+149){int pages=Math.max(1,(filtered.size()+23)/24);page=Math.round((my-y-29)/119F*(pages-1));filter(false);return;}
        for(int i=0;i<24&&page*24+i<filtered.size();i++){int cx=x+5+i%6*40,cy=y+5+i/6*39;if(mx>=cx&&mx<cx+39&&my>=cy&&my<cy+38){Entry e=filtered.get(page*24+i);if(isShiftKeyDown())mc.displayGuiScreen(new GuiMaidSkinDetails(this,e));else apply(e);return;}}
    }
    void apply(Entry entry){if(entry.easterEgg)return;NetworkHandler.channel.sendToServer(new MessageMaidSkin(maid,entry.id,entry.soundPack));selected=entry.id;}
    void back(){mc.displayGuiScreen(null);NetworkHandler.channel.sendToServer(new MessageOpenMaidGui(maid.getEntityId(),0));}
    @Override protected void keyTyped(char c,int key){if(key==1){back();return;}if(search.textboxKeyTyped(c,key)){query=search.getText();filter(true);}}
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0){page+=wheel<0?1:-1;filter(false);}}
    @Override public void onGuiClosed(){icons.clear();Keyboard.enableRepeatEvents(false);lastPack=pack;lastSearch=query;lastPage=page;}
    @Override public boolean doesGuiPauseGame(){return false;}
}
