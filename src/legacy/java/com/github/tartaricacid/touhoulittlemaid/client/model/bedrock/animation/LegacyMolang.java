package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Sandboxed expression subset, not a JavaScript engine. Unsupported names fail at load time. */
public final class LegacyMolang {
    public interface Expression { double eval(Context context); }
    public static final class Context {
        public LegacyAnimationFrame frame;
        public double time;
        public java.util.Map<String,double[]> boneRotations=new java.util.HashMap<String,double[]>();
        public java.util.Map<String, Double> variables = new java.util.HashMap<String, Double>();
        public java.util.Map<String, LegacySecondaryMotion> physics = new java.util.HashMap<String, LegacySecondaryMotion>();
        public java.util.Random random = new java.util.Random(0);
        public Context(LegacyAnimationFrame frame, double time) { this.frame = frame; this.time = time; }
    }
    private LegacyMolang() { }
    private static final class Variable implements Expression {final String key;Variable(String key){this.key=key;}public double eval(Context c){Double n=c.variables.get(key);return n==null?0:n;}}
    private static final class Text implements Expression {final String value;Text(String value){this.value=value;}public double eval(Context c){return 0;}}
    public static Expression constant(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException("Non-finite animation number");
        return new Expression() { public double eval(Context c) { return value; } };
    }
    public static Expression compile(String source) {
        if (source == null || source.length() > 8192) throw new IllegalArgumentException("Expression length");
        Parser parser = new Parser(source.toLowerCase(Locale.ROOT));
        Expression expression = parser.program();
        parser.space();
        if (parser.at != parser.text.length()) throw parser.error("Unsupported expression syntax");
        return expression;
    }
    private static final class Parser {
        final String text;
        int at, depth, nodes;
        Parser(String text) { this.text = text; }
        void space() { while (at < text.length() && Character.isWhitespace(text.charAt(at))) at++; }
        boolean take(String s) { space(); if (!text.startsWith(s, at)) return false; at += s.length(); return true; }
        void expect(String s) { if (!take(s)) throw error("Expected " + s); }
        IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at " + at + ": " + text); }
        void limit() { if (++nodes > 1024) throw error("Expression complexity"); }
        Expression program() {
            final List<Expression> statements = new ArrayList<Expression>();
            while (true) {
                space(); if (at == text.length() || text.charAt(at)=='}') break;
                boolean returns = text.startsWith("return ", at);
                if (returns) at += 7;
                final Expression statement = assignment();
                statements.add(statement);
                if (returns) { take(";"); break; }
                if (!take(";")) break;
            }
            return new Expression() { public double eval(Context c) {
                double result=0; for(Expression statement:statements)result=statement.eval(c);return result;
            }};
        }
        Expression assignment() {
            limit();
            space(); int saved=at;
            while(at<text.length()&&(Character.isLetterOrDigit(text.charAt(at))||text.charAt(at)=='_'||text.charAt(at)=='.'))at++;
            final String name=text.substring(saved,at);
            space();
            if((name.startsWith("v.")||name.startsWith("variable."))&&at<text.length()&&text.charAt(at)=='='&&(at+1==text.length()||text.charAt(at+1)!='=')) {
                at++; final Expression value=assignment();final String key=name.substring(name.indexOf('.')+1);
                return new Expression(){public double eval(Context c){double n=LegacyKeyframeClip.finite(value.eval(c));c.variables.put(key,n);return n;}};
            }
            at=saved;return conditional();
        }
        Expression conditional() {
            if (++depth > 64) throw error("Expression nesting");
            Expression condition = logicalOr();
            if(take("??")) {
                final Expression first=condition, fallback=conditional();
                condition=new Expression(){public double eval(Context c){return first instanceof Variable&&!c.variables.containsKey(((Variable)first).key)?fallback.eval(c):first.eval(c);}};
            }
            if (take("?")) {
                final Expression test = condition, yes = assignment();
                final Expression no = take(":") ? assignment() : constant(0);
                condition = new Expression() { public double eval(Context c) { return test.eval(c) != 0 ? yes.eval(c) : no.eval(c); } };
            }
            depth--; return condition;
        }
        Expression logicalOr() { Expression a = logicalAnd(); while (take("||")) a = binary("||", a, logicalAnd()); return a; }
        Expression logicalAnd() { Expression a = equality(); while (take("&&")) a = binary("&&", a, equality()); return a; }
        Expression equality() {
            Expression a = compare();
            while (true) { if (take("==")) a = binary("==", a, compare()); else if (take("!=")) a = binary("!=", a, compare()); else return a; }
        }
        Expression compare() {
            Expression a = add();
            while (true) { if (take("<=")) a = binary("<=", a, add()); else if (take(">=")) a = binary(">=", a, add());
                else if (take("<")) a = binary("<", a, add()); else if (take(">")) a = binary(">", a, add()); else return a; }
        }
        Expression add() {
            Expression a = multiply();
            while (true) { if (take("+")) a = binary("+", a, multiply()); else if (take("-")) a = binary("-", a, multiply()); else return a; }
        }
        Expression multiply() {
            Expression a = unary();
            while (true) { if (take("*")) a = binary("*", a, unary()); else if (take("/")) a = binary("/", a, unary());
                else if (take("%")) a = binary("%", a, unary()); else return a; }
        }
        Expression unary() {
            limit();
            if (take("+")) return unary();
            if (take("-")) { final Expression a = unary(); return new Expression() { public double eval(Context c) { return -a.eval(c); } }; }
            if (take("!")) { final Expression a = unary(); return new Expression() { public double eval(Context c) { return a.eval(c) == 0 ? 1 : 0; } }; }
            if (take("(")) { Expression a = assignment(); expect(")"); return a; }
            if(take("{")){Expression a=program();expect("}");return a;}
            space(); int begin = at;
            if(at<text.length() && (text.charAt(at)==39 || text.charAt(at)==34)) {
                char quote=text.charAt(at++);int start=at;while(at<text.length()&&text.charAt(at)!=quote)at++;
                if(at==text.length())throw error("Unclosed string");String value=text.substring(start,at++);return new Text(value);
            }
            if (at < text.length() && (Character.isDigit(text.charAt(at)) || text.charAt(at) == '.')) {
                while (at < text.length() && (Character.isDigit(text.charAt(at)) || text.charAt(at) == '.')) at++;
                if (at < text.length() && text.charAt(at) == 'e') {
                    at++; if (at < text.length() && (text.charAt(at) == '+' || text.charAt(at) == '-')) at++;
                    while (at < text.length() && Character.isDigit(text.charAt(at))) at++;
                }
                try { return constant(Double.parseDouble(text.substring(begin, at))); }
                catch (NumberFormatException ex) { throw error("Invalid number"); }
            }
            while (at < text.length() && (Character.isLetterOrDigit(text.charAt(at)) || text.charAt(at) == '_' || text.charAt(at) == '.')) at++;
            if (at == begin) throw error("Expected a value");
            String name = text.substring(begin, at);
            if (take("(")) {
                List<Expression> args = new ArrayList<Expression>();
                if (!take(")")) { do { args.add(assignment()); } while (take(",")); expect(")"); }
                if("ysm.bone_rot".equals(name)){
                    if(args.size()!=1||!(args.get(0) instanceof Text))throw error("Bone name required");
                    expect(".");final int axis=take("x")?0:take("y")?1:take("z")?2:-1;if(axis<0)throw error("Bone axis required");
                    final String bone=((Text)args.get(0)).value;
                    return new Expression(){public double eval(Context c){double[] v=c.boneRotations.get(bone);return v==null?0:v[axis];}};
                }
                return function(name, args);
            }
            return variable(name);
        }
        Expression binary(final String op, final Expression a, final Expression b) {
            limit();
            return new Expression() { public double eval(Context c) {
                double x = a.eval(c);
                if ("||".equals(op)) return x != 0 || b.eval(c) != 0 ? 1 : 0;
                if ("&&".equals(op)) return x != 0 && b.eval(c) != 0 ? 1 : 0;
                double y = b.eval(c);
                if ("+".equals(op)) return x + y; if ("-".equals(op)) return x - y; if ("*".equals(op)) return x * y;
                // SRC ExpressionEvaluatorImpl explicitly defines division by zero as zero.
                if ("/".equals(op)) return y == 0 ? 0 : x / y; if ("%".equals(op)) return x % y;
                if ("<".equals(op)) return x < y ? 1 : 0; if (">".equals(op)) return x > y ? 1 : 0;
                if ("<=".equals(op)) return x <= y ? 1 : 0; if (">=".equals(op)) return x >= y ? 1 : 0;
                if ("==".equals(op)) return x == y ? 1 : 0; return x != y ? 1 : 0;
            } };
        }
        Expression variable(String raw) {
            final String name = raw.startsWith("q.") ? "query." + raw.substring(2) : raw;
            if ("true".equals(name)) return constant(1);
            if ("false".equals(name)) return constant(0);
            if ("math.pi".equals(name)) return constant(Math.PI);
            if ("ysm.first_person_mod_hide".equals(name)) return constant(0); // The maid is never a first-person player model.
            if (name.startsWith("v.") || name.startsWith("variable.")) {
                final String key = name.substring(name.indexOf('.') + 1);
                return new Variable(key);
            }
            final int kind;
            if ("query.anim_time".equals(name)) kind = 0;
            else if ("query.life_time".equals(name)) kind = 1;
            else if ("ysm.head_pitch".equals(name)) kind = 2;
            else if ("ysm.head_yaw".equals(name)) kind = 3;
            else if ("ysm.is_close_eyes".equals(name)) kind = 4;
            else if ("query.is_on_ground".equals(name)) kind = 5;
            else if ("query.is_sitting".equals(name)) kind = 6;
            else if ("query.is_sleeping".equals(name)) kind = 7;
            else if ("query.is_using_item".equals(name)) kind = 8;
            else if ("query.is_jumping".equals(name)) kind = 9;
            else if ("ysm.has_mainhand".equals(name)) kind = 10;
            else if ("ysm.has_offhand".equals(name)) kind = 11;
            else if ("ysm.has_helmet".equals(name)) kind = 12;
            else if ("ysm.has_chest_plate".equals(name)) kind = 13;
            else if ("ysm.has_leggings".equals(name)) kind = 14;
            else if ("ysm.has_boots".equals(name)) kind = 15;
            else if ("query.ground_speed".equals(name)) kind = 16;
            else if ("query.vertical_speed".equals(name)) kind = 17;
            else if ("query.yaw_speed".equals(name)) kind = 18;
            else if ("query.health".equals(name)) kind=19;
            else if ("query.max_health".equals(name)) kind=20;
            else if ("query.head_y_rotation".equals(name)) kind=21;
            else if ("query.head_x_rotation".equals(name)) kind=22;
            else if ("ysm.input_vertical".equals(name)) kind=23;
            else if ("ysm.input_horizontal".equals(name)) kind=24;
            else if("query.is_sneaking".equals(name))kind=25;
            else if("ysm.food_level".equals(name))kind=26;
            else if("tlm.is_sitting".equals(name))kind=6;
            else if("tlm.has_backpack".equals(name))kind=27;
            else if("query.is_in_water".equals(name))kind=28;
            else if("query.is_in_water_or_rain".equals(name))kind=29;
            else if("query.body_y_rotation".equals(name))kind=30;
            else if("ysm.has_elytra".equals(name))return constant(0); // No elytra in vanilla 1.7.
            else throw error("Unsupported variable " + name);
            return new Expression() { public double eval(Context c) {
                switch (kind) {
                    case 0: return c.time;
                    case 1: return c.frame.age / 20D;
                    case 2: return -c.frame.pitch;
                    case 3: return -Math.max(-85, Math.min(85, c.frame.yaw));
                    case 4: double blink = (c.frame.age + Math.abs(c.frame.uuidSeed % 10L)) % 90;
                        return c.frame.sleeping || (85 < blink && blink < 90) ? 1 : 0;
                    case 5: return c.frame.onGround ? 1 : 0;
                    case 6: return c.frame.sitting ? 1 : 0;
                    case 7: return c.frame.sleeping ? 1 : 0;
                    case 9: return !c.frame.riding && !c.frame.onGround && !c.frame.water ? 1 : 0;
                    case 10: return c.frame.mainId.isEmpty() ? 0 : 1;
                    case 11: return c.frame.offId.isEmpty() ? 0 : 1;
                    case 12: return c.frame.helmet ? 1 : 0;
                    case 13: return c.frame.chest ? 1 : 0;
                    case 14: return c.frame.leggings ? 1 : 0;
                    case 15: return c.frame.boots ? 1 : 0;
                    case 16: return c.frame.groundSpeed;
                    case 17: return c.frame.verticalDisplacement * 20;
                    case 18: return c.frame.yawSpeed;
                    case 19: return c.frame.health;
                    case 20: return c.frame.maxHealth;
                    case 21: return c.frame.pitch;
                    case 22: return c.frame.yaw;
                    case 23: return c.frame.inputVertical;
                    case 24: return c.frame.inputHorizontal;
                    case 25:return c.frame.sneaking?1:0;
                    case 26:return c.frame.foodLevel;
                    case 27:return c.frame.backpack?1:0;
                    case 28:return c.frame.water?1:0;
                    case 29:return c.frame.wet?1:0;
                    case 30:return c.frame.bodyYaw;
                    default: return c.frame.using() ? 1 : 0;
                }
            } };
        }
        Expression function(String raw, final List<Expression> a) {
            final String name=raw.startsWith("q.")?"query."+raw.substring(2):raw;
            if("query.is_item_name_any".equals(name)){
                if(a.size()<2||!(a.get(0) instanceof Text))throw error("Item query hand required");
                final boolean left="offhand".equals(((Text)a.get(0)).value);
                return new Expression(){public double eval(Context c){String id=left?c.frame.offId:c.frame.mainId;for(int i=1;i<a.size();i++){if(!(a.get(i) instanceof Text))continue;String wanted=((Text)a.get(i)).value;if(wanted.indexOf(':')<0)wanted="minecraft:"+wanted;if(wanted.equals(id))return 1;}return 0;}};
            }
            if("query.position".equals(name)){
                if(a.size()!=1)throw error("Position axis required");
                return new Expression(){public double eval(Context c){int axis=(int)a.get(0).eval(c);return axis>=0&&axis<3?c.frame.position[axis]:0;}};
            }
            if("query.position_delta".equals(name)) {
                if(a.size()!=1)throw error("Position axis required");
                return new Expression(){public double eval(Context c){int axis=(int)a.get(0).eval(c);return axis>=0&&axis<3?c.frame.positionDelta[axis]:0;}};
            }
            if("ysm.second_order".equals(name)) {
                if(a.size()<2||a.size()>5||!(a.get(0) instanceof Text))throw error("Second order requires name and input");
                final String key=((Text)a.get(0)).value;
                return new Expression(){public double eval(Context c){
                    LegacySecondaryMotion motion=c.physics.get(key);if(motion==null){motion=new LegacySecondaryMotion();c.physics.put(key,motion);}
                    return motion.sample(c.frame.age/20D,a.get(1).eval(c),a.size()>2?a.get(2).eval(c):1,a.size()>3?a.get(3).eval(c):1,a.size()>4?a.get(4).eval(c):1);
                }};
            }
            if("math.random_integer".equals(name)) {
                if(a.size()!=2)throw error("random_integer arity");
                return new Expression(){public double eval(Context c){double lo=Math.ceil(a.get(0).eval(c)),hi=Math.floor(a.get(1).eval(c));return lo+Math.floor(c.random.nextDouble()*Math.max(1,hi-lo+1));}};
            }
            String supported = "|math.sin|math.cos|math.tan|math.asin|math.acos|math.atan|math.abs|math.sqrt|math.exp|math.ln|math.floor|math.ceil|math.round|math.trunc|math.min|math.max|math.pow|math.mod|math.atan2|math.clamp|math.lerp|math.random|";
            if (!supported.contains("|" + name + "|")) throw error("Unsupported function " + name);
            int count = "math.clamp".equals(name) || "math.lerp".equals(name) ? 3
                    : "math.min".equals(name) || "math.max".equals(name) || "math.pow".equals(name) || "math.mod".equals(name) || "math.atan2".equals(name) || "math.random".equals(name) ? 2 : 1;
            if (a.size() != count) throw error("Wrong function arity " + name);
            return new Expression() { public double eval(Context c) {
                double x = a.get(0).eval(c), y = a.size() > 1 ? a.get(1).eval(c) : 0;
                if ("math.random".equals(name)) return x + c.random.nextDouble() * (y - x);
                if ("math.sin".equals(name)) return Math.sin(Math.toRadians(x));
                if ("math.cos".equals(name)) return Math.cos(Math.toRadians(x));
                if ("math.tan".equals(name)) return Math.tan(Math.toRadians(x));
                if ("math.asin".equals(name)) return Math.toDegrees(Math.asin(x));
                if ("math.acos".equals(name)) return Math.toDegrees(Math.acos(x));
                if ("math.atan".equals(name)) return Math.toDegrees(Math.atan(x));
                if ("math.atan2".equals(name)) return Math.toDegrees(Math.atan2(x, y));
                if ("math.abs".equals(name)) return Math.abs(x);
                if ("math.sqrt".equals(name)) return Math.sqrt(x);
                if ("math.exp".equals(name)) return Math.exp(x);
                if ("math.ln".equals(name)) return Math.log(x);
                if ("math.floor".equals(name)) return Math.floor(x);
                if ("math.ceil".equals(name)) return Math.ceil(x);
                if ("math.round".equals(name)) return Math.round(x);
                if ("math.trunc".equals(name)) return x < 0 ? Math.ceil(x) : Math.floor(x);
                if ("math.min".equals(name)) return Math.min(x, y);
                if ("math.max".equals(name)) return Math.max(x, y);
                if ("math.pow".equals(name)) return Math.pow(x, y);
                if ("math.mod".equals(name)) return x % y;
                double z = a.get(2).eval(c);
                if ("math.clamp".equals(name)) return Math.max(y, Math.min(z, x));
                return x + (y - x) * z;
            } };
        }
    }
}
