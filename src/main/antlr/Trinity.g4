grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

prog: (functionDecl | expr)* ;

// Declarations

constDecl: type ID '=' expr ;

functionDecl: type ID '(' formalParameters? ')' block; // Scalar f(Vector x) {...} ;
formalParameters: formalParameter (',' formalParameter)* ;
formalParameter: type ID;

type: TYPE size? ;

// Statements

block: BLOCKSTART expr* BLOCKEND ; // possibly empty statement block

/*stmt: block             // mega nice block scope
    | constDecl
    | 'for' TYPE ID 'in' expr ('by' NUMBER)? block
    | ifBlock
  //  | 'return' expr? ';'
    |  expr ';' // including function call
    ;*/
//stmt: expr ;

// TODO: inconsistent grammar design
ifBlock: ifStmt elseIfStmt* elseStmt? 'end' ;
ifStmt: 'if' expr 'do' expr* ;
elseIfStmt: 'elseif' expr 'do' expr* ;
elseStmt: 'else' 'do' expr*;


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
    | constDecl                         # ConstDeclaration
    //| 'for' type ID 'in' expr ('by' NUMBER)? block #ForLoop
    | 'for' type ID 'in' expr block     #ForLoop
    | ifBlock                           # If
    | block                             # BlockExpression
 //   |  expr ';' // including function call
    ;

exprList: expr (',' expr)* ;

vector: '[' (exprList | range) ']' ;
matrix: vector vector+ ; // [][]...[]
range:   NUMBER '..' NUMBER ;

size: '[' (NUMBER|ID) ',' (NUMBER|ID) ']'     # MatrixSize
    | '[' (NUMBER|ID) ']'                     # VectorSize
    ;
    