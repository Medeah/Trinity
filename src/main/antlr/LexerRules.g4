lexer grammar LexerRules; // note "lexer grammar"

BLOCKSTART: 'do' ;
BLOCKEND:   'end' ;
FOR: 'for' ;
IF: 'if' ;
ELSE: 'else' ;
ELSEIF: 'elseif' ;
RETURN: 'return' ;

TYPE
   :   'Boolean'
   |   'Scalar'
   |   'Vector'
   |   'Matrix'
   ;

BOOL:   'true' | 'false' ;

NUMBER: FLOAT ;

ID  :   [a-zA-Z][a-zA-Z0-9]* ;

RANGE:   INT '..' INT ;

COMMENT : '#' ~'\n'* '\n' -> channel(HIDDEN) ;
WS : [ \t\n\r]+ -> skip ;

fragment FLOAT
    :   '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP            // 1e10 -3e4
    |   '-'? INT                // -3, 45
    ;

fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ;