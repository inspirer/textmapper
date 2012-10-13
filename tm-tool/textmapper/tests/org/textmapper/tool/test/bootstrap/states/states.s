# lapg syntax file

lang = "java"
prefix = "States"
package = "org.textmapper.lapg.test.bootstrap.states"

[initial]

x: /a/ => a

[a]

x: /b/ => b
x: /c/ => c
x: /d/ => d

[b]

x: /a/ => a
x: /c/ => c
x: /d/ => d

[c]

x: /a/ => a
x: /b/ => b
x: /d/ => d

[d]

x: /a/ => a
x: /b/ => b
x: /c/ => c

[a => b, b => c, c => d]

x: /!/

[a,b,c, d => initial]

x: /initialIfD/

[a => d, b => d, c => d]

x: /D/