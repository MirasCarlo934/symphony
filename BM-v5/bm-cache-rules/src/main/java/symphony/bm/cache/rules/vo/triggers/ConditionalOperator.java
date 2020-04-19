package symphony.bm.cache.rules.vo.triggers;

public enum ConditionalOperator {
    EQ {
        @Override
        public boolean evaluate(String value1, String value2) {
            return value1.equals(value2);
        }
    },
    NEQ {
        @Override
        public boolean evaluate(String value1, String value2) {
            return !value1.equals(value2);
        }
    },
    GT {
        @Override
        public boolean evaluate(String value1, String value2) {
            double v1 = Double.parseDouble(value1);
            double v2 = Double.parseDouble(value2);
            return v1 > v2;
        }
    },
    LT {
        @Override
        public boolean evaluate(String value1, String value2) {
            double v1 = Double.parseDouble(value1);
            double v2 = Double.parseDouble(value2);
            return v1 < v2;
        }
    },
    GTE {
        @Override
        public boolean evaluate(String value1, String value2) {
            double v1 = Double.parseDouble(value1);
            double v2 = Double.parseDouble(value2);
            return v1 >= v2;
        }
    },
    LTE {
        @Override
        public boolean evaluate(String value1, String value2) {
            double v1 = Double.parseDouble(value1);
            double v2 = Double.parseDouble(value2);
            return v1 <= v2;
        }
    };
    
    public abstract boolean evaluate(String value1, String value2);
}
