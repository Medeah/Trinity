grammar Trinity;
import LexerRules; // includes all rules from LexerRules.g4

prog:   stmt+ ;

stmt:   decl
    |   forStmt
    |   ifStmt
    |   expr ';'
    ;

decl:   constDecl ';'
    |   funcDecl
    ;

expr:   expr '^'<assoc=right> expr  // exponent
    |   expr ('*'|'/') expr
    |   expr ('+'|'-') expr
    |   expr ('=='|'!=') expr
    |   expr ('and'|'or') expr
    |   '(' expr ')'
    |   NUMBER
    |   BOOL
    |   ID
    |   funcCall
    |   matrix
    |   matrixAccess
    |   matrixTranspose
    ;

exprList: expr (',' expr)* ;
typedIDList: TYPE ID (',' TYPE ID)* ;

// TODO: vector is not used indepenently (maybe remove rule)
vector: '[' (exprList | RANGE) ']' ;
matrix: vector+ ;
// TODO: Subscript and transpose might need refactor
matrixAccess: ID matrix ;
matrixTranspose: ID '\'' ;

funcCall: ID '(' exprList ')' ;

// Declarations
constDecl: TYPE ID '=' expr ;

// TODO: TYPE ID might need to be special for functions? (also in funcCall)
funcDecl: TYPE ID '(' typedIDList? ')' funcDeclBlock ;

// Statements
// NOTE: remember the 'by' option should probably only work for integers
forStmt: FOR TYPE ID 'in' expr ('by' NUMBER)? block ;

ifStmt: IF expr ifBlock ;

// Blocks
// NOTE: only used by forStmt (forBlock?)
block:   BLOCKSTART stmt+ BLOCKEND ;

funcDeclBlock: BLOCKSTART (RETURN? stmt)+ BLOCKEND ;

// TODO: 'then' is BLOCKSTART for if blocks?
ifBlock: 'then' stmt+ (ELSEIF expr 'then' stmt+)* (ELSE stmt+)? BLOCKEND ;
