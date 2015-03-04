grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

prog: (functionDecl | constDecl | stmt)+ ;

// Declarations

constDecl: TYPE ID '=' expr ';';

functionDecl: TYPE ID '(' formalParameters? ')' block; // "void f(int x) {...}" ;
formalParameters: formalParameter (',' formalParameter)* ;
formalParameter: TYPE ID;

// Statements

block: BLOCKSTART stmt* BLOCKEND ; // possibly empty statement block

stmt: block             // mega nice block scope
    | constDecl
    | 'for' TYPE ID 'in' expr ('by' NUMBER)? block
    //| 'if' expr block ('else' block)?
    | 'if' expr ifblock
    | 'return' expr? ';'
    |  expr ';' // including function call
    ;

// TODO: inconsistent grammar design
ifblock: ifblockStart ('elseif' expr ifblockStart)* ('else' ifblockStart)? BLOCKEND;
ifblockStart: 'do' stmt*;

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
    | expr ('<'|'>'|'<='|'>=')      # Relation
    | expr ('=='|'!=') expr         # Equality
    | expr 'and' expr               # And
    | expr 'or' expr                # Or
    | ID                            # Const
    | NUMBER                        # Number
    | BOOL                          # Boolean
    | vector                        # VectorLit // TODO: naming
    | matrix                        # MatrixLit
    | '(' expr ')'                  # Parens
    ;

exprList: expr (',' expr)* ;

vector: '[' (exprList | RANGE) ']' ;
matrix: vector vector+ ; // [][]...[]

