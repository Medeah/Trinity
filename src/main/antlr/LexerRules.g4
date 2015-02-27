lexer grammar LexerRules; // note "lexer grammar"

BLOCKSTART: 'do' ;
BLOCKEND: 'end' ;

INT :   [0-9]+ ;
BOOL:   'true'
   |    'false' ;

TYPE:   'Boolean'
    |   'Scalar'
    |   'Vector'
    |   'Matrix' ;
ID  :   [a-zA-Z][a-zA-Z0-9]* ;

COMMENT : '#' ~'\n'* '\n' -> channel(HIDDEN) ;
WS : [ \t\n\r]+ -> channel(HIDDEN) ;