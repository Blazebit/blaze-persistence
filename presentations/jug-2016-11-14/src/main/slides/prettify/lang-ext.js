var FLOW_CONTROL_KEYWORDS = ["break,continue,do,else,for,if,return,while"];
var C_KEYWORDS = [FLOW_CONTROL_KEYWORDS,"auto,case,char,const,default," +
  "double,enum,extern,float,goto,inline,int,long,register,short,signed," +
  "sizeof,static,struct,switch,typedef,union,unsigned,void,volatile"];
var COMMON_KEYWORDS = [C_KEYWORDS,"catch,class,delete,false,import," +
  "new,operator,private,protected,public,this,throw,true,try,typeof"];
var JAVA_KEYWORDS = [COMMON_KEYWORDS, "abstract,assert,boolean,byte,extends,finally,final,implements,import," +
  "instanceof,interface,null,native,package,strictfp,super,synchronized," +
  "throws,transient"];
var SQL_KEYWORDS = ["ADD,ALL,ALTER,AND,ANY,APPLY,AS,ASC,AUTHORIZATION,BACKUP,BEGIN,BETWEEN,BREAK,BROWSE,BULK,BY," +
"CASCADE,CASE,CHECK,CHECKPOINT,CLOSE,CLUSTERED,COALESCE,COLLATE,COLUMN,COMMIT,COMPUTE,CONNECT,CONSTRAINT,CONTAINS,CONTAINSTABLE," +
"CONTINUE,CONVERT,CREATE,CROSS,CURRENT,CURRENT_DATE,CURRENT_TIME,CURRENT_TIMESTAMP,CURRENT_USER,CURSOR,DATABASE,DBCC,DEALLOCATE," +
"DECLARE,DEFAULT,DELETE,DENY,DESC,DISK,DISTINCT,DISTRIBUTED,DOUBLE,DROP,DUMMY,DUMP,ELSE,END,ERRLVL,ESCAPE,EXCEPT,EXEC,EXECUTE,EXISTS," +
"EXIT,FETCH,FILE,FILLFACTOR,FOLLOWING,FOR,FOREIGN,FREETEXT,FREETEXTTABLE,FROM,FULL,FUNCTION,GOTO,GRANT,GROUP,HAVING,HOLDLOCK,IDENTITY," +
"IDENTITYCOL,IDENTITY_INSERT,IF,IN,INDEX,INNER,INSERT,INTERSECT,INTO,IS,JOIN,KEY,KILL,LEFT,LIKE,LINENO,LOAD,MATCH,MATCHED,MERGE,NATURAL," +
"NATIONAL,NOCHECK,NONCLUSTERED,NOCYCLE,NOT,NULL,NULLIF,OF,OFF,OFFSETS,ON,OPEN,OPENDATASOURCE,OPENQUERY,OPENROWSET,OPENXML,OPTION,OR," +
"ORDER,OUTER,OVER,PARTITION,PERCENT,PIVOT,PLAN,PRECEDING,PRECISION,PRIMARY,PRINT,PROC,PROCEDURE,PUBLIC,RAISERROR,READ,READTEXT,RECONFIGURE," +
"REFERENCES,REPLICATION,RESTORE,RESTRICT,RETURN,RETURNING,REVOKE,RIGHT,ROLLBACK,ROWCOUNT,ROWGUIDCOL,ROWS?,RULE,SAVE,SCHEMA,SELECT," +
"SESSION_USER,SET,SETUSER,SHUTDOWN,SOME,START,STATISTICS,SYSTEM_USER,TABLE,TEXTSIZE,THEN,TO,TOP,TRAN,TRANSACTION,TRIGGER,TRUNCATE," +
"TSEQUAL,UNBOUNDED,UNION,UNIQUE,UNPIVOT,UPDATE,UPDATETEXT,USE,USER,USING,VALUES,VARYING,VIEW,WAITFOR,WHEN,WHERE,WHILE,WITH,WITHIN," +
"WRITETEXT,XML"];
// Max nesting is 2
var OPTIONAL_TYPE_PARAMETER = '(?:<[^<>]+(?:<[^<>]+>)?>)?';
var TYPE = '(?:[@_]?[A-Z]+[a-z][A-Za-z_$@0-9]*' + OPTIONAL_TYPE_PARAMETER + '|void|boolean|byte|char|short|int|long|float|double)';
PR.registerLangHandler(
	PR.createSimpleLexer(
		[
			["str", /^(?:\'(?:[^\\\'\r\n]|\\.)*(?:\'|$)|\"(?:[^\\\"\r\n]|\\.)*(?:\"|$))/, null, '"\''],
			["pln", /^\s+/, null, ' \r\n\t\xA0']
		],
		[
			["com", /^\/\/[^\r\n]*/, null],
			["com", /^\/\*[\s\S]*?(?:\*\/|$)/, null],
			["kwd", new RegExp('^(?:' + ("" + JAVA_KEYWORDS).replace(/^ | $/g, '').replace(/[\s,]+/g, '|') + ')\\b'), null],
			["ann", /^@[a-z_$][a-z_$@0-9]*/i, null],
			["con", /^[A-Z_$][A-Z_$@0-9]+/, null],
			["lang-mdc", new RegExp(
				'^' +
				TYPE + '\\s+' +
				// Method name
				'([a-z_$][a-zA-Z_$@0-9]+)' +
				// Opening parenthesis
				'\\s*\\('
			), null],
			["lang-fdc", new RegExp(
				'^' +
				TYPE + '\\s+' +
				// Field name
				'([a-z_$][a-zA-Z_$@0-9]+)' +
				// End of field
				';'
			), null],
			["typ", /^(?:[@_]?[A-Z]+[a-z][A-Za-z_$@0-9]*|\w+_t\b)/, null],
			["pln", /^[a-z_$][a-z_$@0-9]*/i, null],
			["lit",
			 new RegExp(
				 '^(?:'
				 // A hex number
				 + '0x[a-f0-9]+'
				 // or an octal or decimal number,
				 + '|(?:\\d(?:_\\d+)*\\d*(?:\\.\\d*)?|\\.\\d\\+)'
				 // possibly in scientific notation
				 + '(?:e[+\\-]?\\d+)?'
				 + ')'
				 // with an optional modifier like UL for unsigned long
				 + '[a-z]*', 'i'),
			 null, '0123456789'],
			["pln", /^\\[\s\S]?/, null],
			["pun", new RegExp('^.[^\\s\\w.$@\'"`/\\\\]*'), null]
		]
	), ['javaext']
);
PR.registerLangHandler(
	PR.createSimpleLexer(
		[
			// Whitespace
			["pln", /^[\t\n\r \xA0]+/, null, '\t\n\r \xA0'],
			// A double or single quoted, possibly multi-line, string.
			["str", /^(?:"(?:[^\"\\]|\\.)*"|'(?:[^\'\\]|\\.)*')/, null, '"\'']
		],
		[
			// A comment is either a line comment that starts with two dashes, or
			// two dashes preceding a long bracketed block.
			["com", /^(?:--[^\r\n]*|\/\*[\s\S]*?(?:\*\/|$))/],
			["kwd", new RegExp('^(?:' + ("" + SQL_KEYWORDS).replace(/^ | $/g, '').replace(/[\s,]+/g, '|') + ')(?=[^\\w-]|$)', "i"), null],
			["prm", /^:[a-z_$][a-z_$@0-9]*/i, null],
			// A number is a hex integer literal, a decimal real literal, or in
			// scientific notation.
			["lit", /^[+-]?(?:0x[\da-f]+|(?:(?:\.\d+|\d+(?:\.\d*)?)(?:e[+\-]?\d+)?))/i],
			// An identifier
			["pln", /^[a-z_][\w-]*/i],
			// A run of punctuation
			["pun", /^[^\w\t\n\r \xA0\"\'][^\w\t\n\r \xA0+\-\"\']*/]
		]),
	['sqlext']);
PR.registerLangHandler(
	PR.createSimpleLexer([],[
		["mdc", /^.+/, null],
	]), ['mdc']
);
PR.registerLangHandler(
	PR.createSimpleLexer([],[
		["fdc", /^.+/, null],
	]), ['fdc']
);