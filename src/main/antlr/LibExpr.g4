grammar LibExpr;
import LexerRules; // includes all rules from LexerRules.g4

// The start rule; begin parsing here.

//TODO: probably refactor dcls (declarations) so not part of stmt
prog:   (stmt)+ ;

stmt:   dcl
    |   forStmt
    |   ifStmt
    |   expr ';'
    ;

dcl:    constDcl
    |   funcDcl
    ;

expr:   expr ('*'|'/') expr
    |   expr ('+'|'-') expr
    |   expr ('=='|'!=') expr
    |   INT
    |   BOOL
    |   funcCall
    |   ID
    |   '(' expr ')'
    |   ('[' exprList ']')+
    ;

exprList: expr (',' expr)*;

funcCall: ID '(' exprList ')';

constDcl: TYPE ID '=' expr ';';

//TODO: TYPE ID might need to be special for functions? (also in funcCall)
funcDcl:   TYPE ID '(' (TYPE ID)? (',' TYPE ID)* ')' funcDclBlock;

forStmt:   'for' TYPE ID 'in' expr block;

// TODO: if block should maybe be "then .. end" not "do .. end"
ifStmt: 'if' expr ifBlock;

block:   BLOCKSTART stmt+ BLOCKEND;

funcDclBlock: BLOCKSTART ('return'? stmt)+ BLOCKEND;

// TODO: the elseif could probably be refactored to use the ifstmt..
ifBlock: 'then' stmt+ ('elseif' expr 'then' stmt+)* ('else' stmt+)? BLOCKEND;
