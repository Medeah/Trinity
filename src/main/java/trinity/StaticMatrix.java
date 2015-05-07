package trinity;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

// TODO: become independent of this class
public class StaticMatrix {
    //public List<TrinityParser.ExprContext> items;
    public List<ParserRuleContext> items;
    public TrinityParser.RangeContext range;
    public String id;
    //public MatrixType type;
}
