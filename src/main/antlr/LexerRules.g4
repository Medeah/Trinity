lexer grammar LexerRules; // note "lexer grammar"

BOOL:   'true' | 'false' ;

NUMBER:   INT ('.' [0-9]+)? EXP? ; // 3, 1.35, 1.3e9, 1E10

ID  :   [a-zA-Z][a-zA-Z0-9]* ;

COMMENT : '#' ~'\n'* -> channel(HIDDEN) ;
WS : [ \t\n\r]+ -> skip ;

fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ;
