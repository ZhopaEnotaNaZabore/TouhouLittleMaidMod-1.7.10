package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
public final class MaidSkinRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void run() throws Exception {
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);
        EntityPlayerMP owner=(EntityPlayerMP)MultiblockRegression.unsafe().allocateInstance(EntityPlayerMP.class);
        m.worldObj=MultiblockRegression.world();owner.worldObj=m.worldObj;m.owner=owner;m.alive=true;m.setEntityId(12);
        java.lang.reflect.Field uuid=Entity.class.getDeclaredField("entityUniqueID");uuid.setAccessible(true);UUID original=UUID.randomUUID();uuid.set(m,original);
        m.model="before";m.sound="old:voice";
        MessageMaidSkin packet=roundtrip(new MessageMaidSkin(m,"geckolib:sta","peco:default"));
        check(packet.apply(m,owner)&&m.model.equals("geckolib:sta")&&m.sound.equals("peco:default"),"model and linked voice applied");
        packet=roundtrip(new MessageMaidSkin(m,"external_pack:custom/model",""));
        check(packet.apply(m,owner)&&m.sound.equals("peco:default"),"custom pack allowed; absent voice retains current voice");
        m.posX=8;check(!packet.apply(m,owner),"distance boundary rejected");m.posX=0;
        m.owner=null;check(!packet.apply(m,owner),"nonowner rejected");m.owner=owner;
        m.alive=false;check(!packet.apply(m,owner),"dead maid rejected");m.alive=true;
        uuid.set(m,UUID.randomUUID());check(!packet.apply(m,owner),"recycled entity ID rejected");uuid.set(m,original);
        m.setEntityId(13);check(!packet.apply(m,owner),"wrong entity rejected");m.setEntityId(12);
        owner.worldObj=MultiblockRegression.world();check(!packet.apply(m,owner),"different world rejected");owner.worldObj=m.worldObj;
        String old=m.model;
        check(!roundtrip(new MessageMaidSkin(m,"bad id","valid:voice")).apply(m,owner)&&m.model.equals(old),"invalid model cannot mutate state");
        check(!roundtrip(new MessageMaidSkin(m,"valid:model","bad voice")).apply(m,owner)&&m.model.equals(old),"invalid voice fails atomically");
        check(!MessageMaidSkin.validId("")&&!MessageMaidSkin.validId("X:Upper")&&MessageMaidSkin.validId("pack:model_123"),"resource identifier syntax");
        ByteBuf b=Unpooled.buffer();try{b.writeInt(12).writeLong(0).writeLong(0).writeShort(193);boolean failed=false;try{new MessageMaidSkin().fromBytes(b);}catch(IllegalArgumentException expected){failed=true;}check(failed,"oversized payload rejected before allocation");}finally{b.release();}
        b=Unpooled.buffer();try{b.writeInt(12).writeLong(0).writeLong(0).writeShort(10).writeByte(1);boolean failed=false;try{new MessageMaidSkin().fromBytes(b);}catch(IllegalArgumentException expected){failed=true;}check(failed,"truncated string rejected");}finally{b.release();}
        System.out.println("Maid skins network regression: "+checks+" checks PASS (owner, UUID, distance, validation, serialization)");
    }
    private static MessageMaidSkin roundtrip(MessageMaidSkin m){ByteBuf b=Unpooled.buffer();try{m.toBytes(b);MessageMaidSkin copy=new MessageMaidSkin();copy.fromBytes(b);return copy;}finally{b.release();}}
    public static final class Maid extends EntityMaid {
        EntityPlayerMP owner;boolean alive;String model,sound;
        private Maid(World world){super(world);}
        @Override public EntityLivingBase getOwner(){return owner;}
        @Override public boolean isEntityAlive(){return alive;}
        @Override public void setModelId(String id){model=id;}
        @Override public void setSoundPackId(String id){sound=id;}
    }
}
