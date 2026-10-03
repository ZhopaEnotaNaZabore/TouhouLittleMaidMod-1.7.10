package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import com.google.gson.*;
import java.util.*;

/** Bounded Molang event programs; never executes JavaScript or host commands. */
public final class LegacyTimeline {
    final List<Event> events = new ArrayList<Event>();
    public LegacyTimeline(JsonObject clip) {
        if (clip.has("timeline")) {
            for (Map.Entry<String,JsonElement> entry:clip.getAsJsonObject("timeline").entrySet()) {
                double time=LegacyKeyframeClip.finite(Double.parseDouble(entry.getKey()));
                if(time<0)throw new IllegalArgumentException("Negative event time");
                Event event=new Event(time);events.add(event);
                if(entry.getValue().isJsonArray())for(JsonElement value:entry.getValue().getAsJsonArray())parse(event,value.getAsString());
                else parse(event,entry.getValue().getAsString());
            }
            Collections.sort(events,new Comparator<Event>(){public int compare(Event a,Event b){return Double.compare(a.time,b.time);}});
        }
    }
    private void parse(Event event,String script){
        if(script.length()>8192)throw new IllegalArgumentException("Timeline statement length");
        if(event.assignments.size()>=1024)throw new IllegalArgumentException("Too many event programs");
        event.assignments.add(new Assignment(null,script));
    }
    static final class Assignment {
        final String name,source;final LegacyMolang.Expression expression;
        Assignment(String name,String source){this.name=name;this.source=source;this.expression=LegacyMolang.compile(source);}
    }
    static final class Event {
        final double time;final List<Assignment> assignments=new ArrayList<Assignment>();
        Event(double time){this.time=time;}
    }
    public Playback playback(long seed){return new Playback(seed);}
    public final class Playback {
        final LegacyMolang.Context context=new LegacyMolang.Context(null,0);
        private final long seed;private int next;private long cycle;private double last=-1;
        Playback(long seed){this.seed=seed;context.random=new Random(seed);}
        public LegacyMolang.Context advance(double elapsed,double length,boolean loop,LegacyAnimationFrame frame){
            elapsed=Math.max(0,elapsed);
            if(elapsed<last){next=0;cycle=0;context.variables.clear();context.random=new Random(seed);}
            last=elapsed;context.frame=frame;
            long target=loop&&length>0?(long)Math.floor(elapsed/length):0;
            if(events.isEmpty()){context.time=loop&&length>0?elapsed%length:Math.min(elapsed,length>0?length:elapsed);return context;}
            // Returning to a maid after a long visibility gap must not permanently
            // disable its physics clip. Resync at the current loop, with bounded work.
            if(target-cycle>1024){cycle=target;next=0;}
            while(cycle<target){execute(length);next=0;cycle++;}
            double local=loop&&length>0?elapsed-cycle*length:Math.min(elapsed,length>0?length:elapsed);
            execute(local);context.time=local;return context;
        }
        private void execute(double until){
            while(next<events.size()&&events.get(next).time<=until){
                Event event=events.get(next++);context.time=event.time;
                for(Assignment assignment:event.assignments)
                    {double value=LegacyKeyframeClip.finite(assignment.expression.eval(context));if(assignment.name!=null)context.variables.put(assignment.name,value);}
            }
        }
    }
}
