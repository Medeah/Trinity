grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

prog: (functionDecl | stmt)* ;

// Declarations

constDecl: type ID '=' semiExpr;

functionDecl: type ID '(' formalParameters? ')' 'do' block 'end' ;
formalParameters: formalParameter (',' formalParameter)* ;
formalParameter: type ID ;


type: ('Boolean' | 'Scalar')                        # PrimitiveType
    | 'Vector' '[' (NUMBER|ID) ']'                  # VectorType
    | 'Matrix' '[' (NUMBER|ID) ',' (NUMBER|ID) ']'  # MatrixType ;

// Statements

block: stmt* ('return' semiExpr)? ;

semiExpr: expr LINETERMINATOR;

stmt: constDecl                                 # ConstDeclaration
    | semiExpr                                  # SingleExpression
    | 'for' type ID 'in' expr 'do' block 'end'  # ForLoop
    | 'if' expr 'then' block
      ('elseif' expr 'then' block)*
      ('else' block)? 'end'                     # IfStatement
    | 'do' block 'end'                          # BlockStatement
    ;

// Expressions

expr: ID '(' exprList? ')'              # FunctionCall
    | ID '[' expr ']'                   # SingleIndexing
    | ID '[' expr ',' expr ']'          # DoubleIndexing
    | '-' expr                          # Negate
    | '!' expr                          # Not
    | expr '\''                         # Transpose
    | <assoc=right> expr op='^' expr    # Exponent
    | expr op=('*'|'/'|'%') expr        # MultDivMod
    | expr op=('+'|'-') expr            # AddSub
    | expr op=('<'|'>'|'<='|'>=') expr  # Relation
    | expr op=('=='|'!=') expr          # Equality
    | expr op='and' expr                # And
    | expr op='or' expr                 # Or
    | ID                                # Identifier
    | NUMBER                            # Number
    | BOOL                              # Boolean
    | matrix                            # MatrixLit
    | vector                            # VectorLit
    | '(' expr ')'                      # Parens
    ;

exprList: expr (',' expr)* ;

vector: '[' (exprList | range) ']' ;
matrix: vector vector+ ;
range:   NUMBER '..' NUMBER ;
