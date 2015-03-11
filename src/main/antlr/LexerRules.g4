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

COMMENT : '#' ~'\n'* -> channel(HIDDEN) ;
WS : [ \t\n\r]+ -> skip ;


fragment FLOAT:  '-'? INT ('.' [0-9]+)? EXP? ; // 3, 1.35, 1.3e9, 1E10
fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ;