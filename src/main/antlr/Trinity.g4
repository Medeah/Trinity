grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

prog: (functionDecl | stmt)* ;

// Declarations

constDecl: type ID '=' semiExpr;

functionDecl: type ID '(' formalParameters? ')' 'do' block 'end' ;
formalParameters: formalParameter (',' formalParameter)* ;
formalParameter: type ID ;

type: TYPE size? ;

// Statements

block: stmt* ('return' semiExpr)? ;

semiExpr: expr LINETERMINATOR;

stmt: constDecl                       # ConstDeclaration
    | semiExpr                         # SingleExpression
    | 'for' type ID 'in' expr 'do' block 'end'      # ForLoop
    | 'if' expr 'then' block
      ('elseif' expr 'then' block)*
      ('else' block)? 'end'                         # IfStatement
    | 'do' block 'end'                              # BlockStatement
    ;

// Expressions

// TODO: no sub-matrix sub-vector indexing (range) for now (maybe we don't need it)
expr: ID '(' exprList? ')'              # FunctionCall
    | ID '[' expr ']'                   # VectorIndexing
    | ID '[' expr ',' expr ']'          # MatrixIndexing
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
    | vector                            # VectorLit // TODO: naming
    | '(' expr ')'                      # Parens
 //   |  expr ';' // including function call
    ;

exprList: expr (',' expr)* ;

vector: '[' (exprList | range) ']' ;
matrix: vector vector+ ; // [][]...[]
range:   NUMBER '..' NUMBER ;

size: '[' (NUMBER|ID) ',' (NUMBER|ID) ']'     # MatrixSize
    | '[' (NUMBER|ID) ']'                     # VectorSize
    ;
    