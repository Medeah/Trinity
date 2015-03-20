grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

prog: (functionDecl | stmt)* ;

// Declarations

constDecl: TYPE ID '=' expr ';';

functionDecl: TYPE ID '(' formalParameters? ')' block; // Scalar f(Vector x) {...} ;
formalParameters: formalParameter (',' formalParameter)* ;
formalParameter: TYPE ID;

// Statements

block: BLOCKSTART stmt* BLOCKEND ; // possibly empty statement block

stmt: block             // mega nice block scope
    | constDecl
    | 'for' TYPE ID 'in' expr ('by' NUMBER)? block
    | ifBlock
    | 'return' expr? ';'
    |  expr ';' // including function call
    ;

// TODO: inconsistent grammar design
ifBlock: ifStmt elseIfStmt* elseStmt? 'end' ;
ifStmt: 'if' expr 'do' stmt* ;
elseIfStmt: 'elseif' expr 'do' stmt* ;
elseStmt: 'else' 'do' stmt*;


// Expressions

// TODO: no sub-matrix sub-vector indexing (range) for now (maybe we don't need it)
expr: ID '(' exprList? ')'          # FunctionCall
    | expr '[' expr ']'             # VectorIndexing
    | expr '[' expr ',' expr ']'    # MatrixIndexing
    | '-' expr                      # Negate
    | '!' expr                      # Not
    | expr '\''                     # Transpose
    | <assoc=right> expr '^' expr   # Exponent
    | expr ('*'|'/'|'%') expr       # MultDivMod
    | expr ('+'|'-') expr           # AddSub
    | expr ('<'|'>'|'<='|'>=') expr # Relation
    | expr ('=='|'!=') expr         # Equality
    | expr 'and' expr               # And
    | expr 'or' expr                # Or
    | ID                            # Const
    | NUMBER                        # Number
    | BOOL                          # Boolean
    | matrix                        # MatrixLit
    | vector                        # VectorLit // TODO: naming
    | '(' expr ')'                  # Parens
    ;

exprList: expr (',' expr)* ;

vector: '[' (exprList | RANGE) ']' ;
matrix: vector vector+ ; // [][]...[]

