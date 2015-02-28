grammar LibExpr;
import LexerRules; // includes all rules from LexerRules.g4

// The start rule; begin parsing here.

//TODO: probably refactor dcls (declarations) so not part of stmt
prog:   (stmt)+ ;

stmt:   decl
    |   forStmt
    |   ifStmt
    |   expr ';'
    ;

decl:    constDecl
    |   funcDecl
    ;

expr:   expr ('*'|'/') expr
    |   expr ('+'|'-') expr
    |   expr ('=='|'!=') expr
    |   expr ('and'|'or') expr
    |   FLOAT
    |   BOOL
    |   funcCall
    |   ID
    |   '(' expr ')'
    |   ('[' exprList ']')+
    |   ('[' RANGE ']')
    ;

exprList: expr (',' expr)* ;

funcCall: ID '(' exprList ')' ;

constDecl: TYPE ID '=' expr ';' ;

//TODO: TYPE ID might need to be special for functions? (also in funcCall)
funcDecl:   TYPE ID '(' (TYPE ID)? (',' TYPE ID)* ')' funcDclBlock ;

//TODO: FLOAT should maybe be INT here, or expr depending on what we want to support.
forStmt:   'for' TYPE ID 'in' expr ('by' FLOAT)? block ;

// TODO: if block should maybe be "then .. end" not "do .. end"
ifStmt: 'if' expr ifBlock ;

block:   BLOCKSTART stmt+ BLOCKEND ;

funcDclBlock: BLOCKSTART ('return'? stmt)+ BLOCKEND ;

// TODO: the elseif could probably be refactored to use the ifstmt..
ifBlock: 'then' stmt+ ('elseif' expr 'then' stmt+)* ('else' stmt+)? BLOCKEND ;
