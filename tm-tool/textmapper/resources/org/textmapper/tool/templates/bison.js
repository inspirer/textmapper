/*
 * EXPERIMENTAL! This file will eventually replace bison.ltp as a template for the Bison target.
 */
function main() {
  write(opts.module + '.y', unit());
}

function unit() {
  return '%{\n' +
      prologue() +
      '%}\n' +
      declarations() +
      '%%\n\n' +
      Java.from(parser.symbols).filter(function (i) {
        return !i.isTerm();
      }).map(function (nterm) {
        return nonterm(nterm);
      }).join('\n') +
      '\n%%\n\n' +
      epilogue();
}

function nonterm(nterm) {
  return symbolName(nterm) + ' :\n  ' +
      nontermRules(nterm).map(function (rule) {
        return rhs(rule);
      }).join('\n | ') + '\n;\n';
}

var ruleCache = null;

function nontermRules(nterm) {
  if (ruleCache === null) {
    ruleCache = {};
    Java.from(syntax.grammar.rules).forEach(function (rule) {
      if (!ruleCache.hasOwnProperty(rule.left.index)) {
        ruleCache[rule.left.index] = [rule];
      } else {
        ruleCache[rule.left.index].push(rule);
      }
    });
  }

  // TODO sort
  return ruleCache[nterm.index];
}

function rhs(rule) {
  return (rule.getRight().length == 0 ? '%empty' : Java.from(rule.getRight()).map(function (sym) {
        return symbolName(sym.target);
      }).join(' ')) + (ruleCode(rule) ? '\n\t\t\t' + parserAction(rule) : '');
}

function parserAction(rule) {
  // TODO
  return ruleCode(rule);
}

//function symText(property) {
//  isVal = property == 'value';
//  suffix = isVal ? '' : '.' + property;
//  return self.rightOffset == -1 ? (isVal ? 'null' : '-1') : (isVal ? '$' : '@') + (self.isLeft ? '$' : self.leftOffset) + suffix;
//}
//
//function symAccess(property) {
//  return (property == 'value' ? type(symbol) ? '((' + type(symbol) + ')' + symText.call(self, property) + ')' : symText.call(self, property) : symText.call(self, property));
//}
//
//function type(sym) {
//  if (sym.type && sym.type.class.name == 'org.textmapper.lapg.builder.LiRawAstType') {
//    return sym.type.toString();
//  }
//  return null;
//}

function prologue() {
  return '#include <stdio.h>\n';
}

function epilogue() {
  return '';
}

function declarations() {
  return '\n%start ' + startSymbol() + '\n\n' +
      tokens() +
      directives();
}

function tokens() {
  return Java.from(syntax.grammar.priorities).map(function (prio) {
        return '%' + prioType(prio) + ' ' + Java.from(prio.symbols).map(function (term) {
              return symbolName(term);
            }).join(' ') + '\n';
      }).join('') +

      nonprioTerms().map(function (term) {
        return '%token ' + symbolName(term) + '\n';
      }).join('') +
      '\n';
}

function prioType(prio) {
  return prio.prio == 1 ? 'left' : prio.prio == 2 ? 'right' : 'nonassoc';
}

function nonprioTerms() {
  var prioTerms = {};
  Java.from(syntax.grammar.priorities).forEach(function (it) {
    Java.from(it.symbols).forEach(function (it) {
      prioTerms[it.index] = true;
    });
  });
  return Java.from(syntax.grammar.symbols).filter(function (it) {
    return it.isTerm() && it.index > 0 && !prioTerms.hasOwnProperty(it.index);
  });
}

function startSymbol() {
  var input = Java.from(syntax.grammar.input);
  if (input.length == 0) return 'input';
  if (input.length == 1) return symbolName(input[0].target);
  return '';
}

function symbolName(sym) {
  var TMDataUtil = Java.type('org.textmapper.tool.compiler.TMDataUtil');
  return TMDataUtil.getId(sym);
}

function ruleCode(rule) {
  var TMDataUtil = Java.type('org.textmapper.tool.compiler.TMDataUtil');
  return TMDataUtil.getCode(rule);
}

function directives() {
  return '%locations\n';
}

