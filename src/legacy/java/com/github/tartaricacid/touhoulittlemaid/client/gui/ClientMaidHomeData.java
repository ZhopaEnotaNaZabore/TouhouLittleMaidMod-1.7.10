package com.github.tartaricacid.touhoulittlemaid.client.gui;

import java.util.HashMap;
import java.util.Map;

/** Client-only snapshot supplied by the authoritative server. */
public final class ClientMaidHomeData {
    private static final Map<Integer, Entry> VALUES=new HashMap<Integer, Entry>();
    private ClientMaidHomeData(){}
    public static void put(int id,Entry value){VALUES.put(id,value);}
    public static Entry get(int id){return VALUES.get(id);}
    public static void clear(){VALUES.clear();}
    public static final class Entry{
        public final int[] work,idle,sleep;public final int dimension;public final boolean configured;
        public Entry(int[] w,int[] i,int[] s,int d,boolean c){work=w;idle=i;sleep=s;dimension=d;configured=c;}
    }
}
