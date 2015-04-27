grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

@header {
import trinity.types.*;
}

prog: (functionDecl | stmt)* ;

// Declarations

constDecl: type ID '=' semiExpr;

functionDecl: type ID '(' formalParameters? ')' 'do' block 'end' ;
formalParameters: formalParameter (',' formalParameter)* ;
formalParameter: type ID ;


type
    : ('Boolean' | 'Scalar')                        # PrimitiveType
    | 'Vector' '[' (NUMBER|ID) ']'                  # VectorType
    | 'Matrix' '[' (NUMBER|ID) ',' (NUMBER|ID) ']'  # MatrixType
    ;

// Statements

block: stmt* ('return' semiExpr)? ;

semiExpr: expr ';';

stmt
    : constDecl                                 # ConstDeclaration
    | semiExpr                                  # SingleExpression
    | 'for' type ID 'in' expr 'do' block 'end'  # ForLoop
    | 'if' expr 'then' block
      ('elseif' expr 'then' block)*
      ('else' block)? 'end'                     # IfStatement
    | 'do' block 'end'                          # BlockStatement
    ;

// Expressions

expr
locals [Type t]
    : ID '(' exprList? ')'              # FunctionCall
    | ID '[' expr ']'                   # SingleIndexing
    | ID '[' expr ',' expr ']'          # DoubleIndexing
    | '-' expr                          # Negate
    | '!' expr                          # Not
    | expr '\''                         # Transpose
    | <assoc=right> expr op='^' expr    # Exponent
    | expr op=('*'|'/') expr            # MultiplyDivide
    | expr op=('+'|'-') expr            # AddSubtract
    | expr op=('<'|'>'|'<='|'>=') expr  # Relation
    | expr op=('=='|'!=') expr          # Equality
    | expr op='and' expr                # And
    | expr op='or' expr                 # Or
    | ID                                # Identifier
    | NUMBER                            # Number
    | BOOL                              # Boolean
    | matrix                            # MatrixLiteral
    | vector                            # VectorLiteral
    | '(' expr ')'                      # Parens
    ;

exprList: expr (',' expr)* ;

vector: '[' (exprList | range) ']' ;
matrix: vector vector+ ;
range:   NUMBER '..' NUMBER ;
