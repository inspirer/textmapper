//  Copyright 2002-2015 Evgeny Gryaznov
// 
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

(function() {

var root = this;
var jsregex = { DEBUG_SYNTAX: false };

jsregex.Tokens = {
  Unavailable_: -1,
  eoi: 0,
  char: 1,
  escaped: 2,
  charclass: 3,
  Dot: 4,
  Mult: 5,
  Plus: 6,
  Questionmark: 7,
  quantifier: 8,
  op_minus: 9,
  op_union: 10,
  op_intersect: 11,
  Lparen: 12,
  Or: 13,
  Rparen: 14,
  LparenQuestionmark: 15,
  Lsquare: 16,
  LsquareXor: 17,
  expand: 18,
  kw_eoi: 19,
  Rsquare: 20,
  Minus: 21
};

jsregex.States = {
  initial: 0,
  afterChar: 1,
  inSet: 2
};

jsregex.Lexer = function(text, errorHandler) {
  this.reset(text);
  this.errorHandler = errorHandler;
  this.tokenLine = 1;
  this.currLine = 1;
  this.currColumn = 1;
  this.currOffset = 0;
};

jsregex.Lexer.prototype = {
  reset: function(text) {
    this.text = text;
    this.state = 0;
    this.chr = text.length > 0 ? text.charCodeAt(0) : 0;
    this.offset = 1;
    this.token = "";
  },

  tmCharClass: [
    0, 1, 1, 1, 1, 1, 1, 1, 1, 23, 23, 1, 1, 23, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 12, 1, 13, 15, 7, 8, 10, 11, 6, 22,
    26, 26, 26, 26, 26, 26, 26, 26, 21, 21, 16, 1, 1, 1, 1, 9,
    1, 28, 28, 28, 29, 28, 28, 20, 20, 20, 20, 20, 20, 20, 20, 20,
    24, 20, 20, 32, 20, 31, 20, 32, 27, 20, 20, 17, 4, 19, 18, 20,
    1, 30, 30, 28, 29, 28, 30, 20, 20, 33, 20, 20, 20, 20, 25, 20,
    5, 20, 25, 34, 25, 31, 25, 32, 27, 20, 20, 2, 14, 3, 1, 1
  ],
  tmStateMap: [
    0, 1, 2
  ],
  tmRuleSymbol: [
    18, 1, 2, 2, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10,
    11, 1, 12, 13, 14, 15, 16, 17, 1, 19, 20, 21, 1
  ],
  tmClassesCount: 35,
  tmGoto: [
    -2, 3, 4, 3, 5, 3, 6, 7, 7, 7, 3, 8, 3, 9, 10, 11,
    3, 12, 3, -1, 3, 3, -1, 3, 3, 3, 3, 3, 3, 3, 3, 3,
    3, 3, 3, -1, 3, 13, 3, 5, 3, 6, 14, 15, 16, 3, 8, 3,
    9, 10, 11, 3, 12, 3, -1, 3, 3, -1, 3, 3, 3, 3, 3, 3,
    3, 3, 3, 3, 3, 3, -1, 3, 3, 3, 5, 3, 6, 7, 7, 7,
    3, 17, 3, 18, 18, 18, 3, -1, 3, 19, 3, 3, -1, 3, 3, 3,
    3, 3, 3, 3, 3, 3, 3, 3, 3, -4, -4, -4, -4, -4, -4, -4,
    -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
    -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
    -4, 20, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
    20, -4, -4, -4, 20, 20, -4, 20, 20, 20, 20, 20, 20, 20, 20, -1,
    21, 21, 21, 21, 22, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
    21, 21, 21, 21, -1, 21, -1, -1, 23, 24, 25, 21, 26, 23, 27, 26,
    21, 26, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12,
    -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12,
    -12, -12, -12, -12, -12, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20,
    -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20,
    -20, -20, -20, -20, -20, -20, -20, -20, -27, -27, -27, -27, -27, -27, -27, -27,
    -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27,
    -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -27, -21, -21, -21, -21, -21,
    -21, -21, -21, -21, 28, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21,
    -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -22, -22,
    -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22,
    -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22,
    -22, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23,
    -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23,
    -23, -23, -23, -23, -25, -25, -25, -25, -25, -25, -25, -25, -25, -25, -25, -25,
    -25, -25, -25, -25, -25, -25, 29, -25, -25, -25, -25, -25, -25, -25, -25, -25,
    -25, -25, -25, -25, -25, -25, -25, -4, -4, -4, -4, -4, 20, -4, -4, 30,
    -4, -4, 31, 32, -4, -4, -4, -4, -4, -4, -4, 20, 33, -4, -4, 20,
    20, 33, 20, 20, 20, 20, 20, 20, 20, 20, -13, -13, -13, -13, -13, -13,
    -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13,
    -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -14, -14, -14,
    -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14,
    -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14,
    -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15,
    -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15,
    -15, -15, -15, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30,
    -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30,
    -30, -30, -30, -30, -30, -30, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31,
    -31, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31, -31,
    -31, -31, -31, -31, -31, -31, -31, -31, -31, -29, -29, -29, -29, -29, -29, -29,
    -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29,
    -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -29, -1, -1, -1, 34,
    -1, 20, -1, -1, -1, -1, -1, 20, -1, -1, -1, -1, -1, -1, -1, -1,
    20, 20, -1, -1, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, -5,
    -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5,
    -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5,
    -5, -5, -1, -1, 35, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6,
    -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6,
    -6, -6, -6, -6, -6, -6, -6, -6, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, 36, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    37, -1, -1, -1, -1, 37, -1, 37, 37, 37, -1, -1, -1, -1, -10, -10,
    -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10,
    -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10,
    -10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, 38, -1, -1, -1, -1, 38, -1, 38, 38, 38,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 39,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, 39, 39, -26, -26, -26, -26, -26, -26, -26, -26, -26,
    -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -26,
    -26, -26, -26, -26, -26, -26, -26, -26, -26, -26, -1, -1, -1, 40, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    41, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 42, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, 43, -1, -1, -1, -1, -1, -1, 44, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, -1, -1, -1, 33, -1, -1,
    -1, -1, -1, -1, -1, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
    -3, -3, -3, -3, -3, -3, -3, -3, -3, -1, -1, -1, -1, -1, 45, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 45, 45, -1,
    -1, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, 46, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, 47, -1, -1, -1, -1, 47, -1, 47, 47, 47, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, 48, -1, -1, -1, -1, 48, -1, 48, 48,
    48, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    39, -1, -1, -1, -1, 49, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, 39, 39, -18, -18, -18, -18, -18, -18, -18, -18,
    -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18,
    -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -17, -17, -17, -17, -17,
    -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17,
    -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -1, -1,
    -1, 50, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16,
    -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16,
    -16, -16, -16, -16, -1, -1, -1, 43, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, 44, -1, -1, -1, -1, 44, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 51, -1, 45, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 45, 45, -1, -1, 45,
    45, 45, 45, 45, 45, 45, 45, 45, 45, 45, -7, -7, -7, -7, -7, -7,
    -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7,
    -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -8, -8, -8,
    -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
    -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, 52, -1, -1, -1, -1, 52, -1, 52, 52, 52, -1,
    -1, -1, -1, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24,
    -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24,
    -24, -24, -24, -24, -24, -24, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19,
    -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19,
    -19, -19, -19, -19, -19, -19, -19, -19, -19, -11, -11, -11, -11, -11, -11, -11,
    -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11,
    -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, 53, -1, -1, -1, -1, 53, -1, 53, 53, 53, -1, -1, -1, -1, -9,
    -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
    -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
    -9, -9
  ],

  mapCharacter: function(chr) {
    if (chr >= 0 && chr < 128) {
      return this.tmCharClass[chr];
    }
    return 1;
  },

  createToken: function(lapg_n, ruleIndex) {
    var spaceToken = false;
    switch (ruleIndex) {
    case 0:
      return this.createExpandToken(lapg_n, ruleIndex);
    case 1: // char: /[^()\[\]\.|\\\/*?+\-]/
       this.quantifierReady(); 
      break;
    case 2: // escaped: /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/
       this.quantifierReady(); 
      break;
    case 3: // escaped: /\\[abfnrtv]/
       this.quantifierReady(); 
      break;
    case 4: // escaped: /\\[0-7][0-7][0-7]/
       this.quantifierReady(); 
      break;
    case 5: // escaped: /\\[xX]{hx}{hx}/
       this.quantifierReady(); 
      break;
    case 6: // escaped: /\\[uU]{hx}{hx}{hx}{hx}/
       this.quantifierReady(); 
      break;
    case 7: // charclass: /\\[wWsSdD]/
       this.quantifierReady(); 
      break;
    case 8: // charclass: /\\p\{\w+\}/
       this.quantifierReady(); 
      break;
    case 9: // '.': /\./
       this.quantifierReady(); 
      break;
    case 10: // '*': /\*/
      this.state = jsregex.States.initial;
      break;
    case 11: // '+': /\+/
      this.state = jsregex.States.initial;
      break;
    case 12: // '?': /\?/
      this.state = jsregex.States.initial;
      break;
    case 13: // quantifier: /\{[0-9]+(,[0-9]*)?\}/
      this.state = jsregex.States.initial;
      break;
    case 14: // op_minus: /\{\-\}/
      this.state = jsregex.States.initial;
      break;
    case 15: // op_union: /\{\+\}/
      this.state = jsregex.States.initial;
      break;
    case 16: // op_intersect: /\{&&\}/
      this.state = jsregex.States.initial;
      break;
    case 17: // char: /[*+?]/
       this.quantifierReady(); 
      break;
    case 18: // '(': /\(/
       this.state = 0; 
      break;
    case 19: // '|': /\|/
       this.state = 0; 
      break;
    case 20: // ')': /\)/
       this.quantifierReady(); 
      break;
    case 21: // '(?': /\(\?[is\-]+:/
       this.state = 0; 
      break;
    case 22: // '[': /\[/
      this.state = jsregex.States.inSet;
      break;
    case 23: // '[^': /\[\^/
      this.state = jsregex.States.inSet;
      break;
    case 24: // char: /\-/
       this.quantifierReady(); 
      break;
    case 26: // ']': /\]/
       this.state = 0; this.quantifierReady(); 
      break;
    }
    return !(spaceToken);
  },

  subTokensOfExpand: {
    '{eoi}': 25
  },

  createExpandToken: function(lapg_n, ruleIndex) {
    if (this.token in this.subTokensOfExpand) {
      ruleIndex = this.subTokensOfExpand[this.token];
      lapg_n.symbol = this.tmRuleSymbol[ruleIndex];
    }
    var spaceToken = false;
    switch(ruleIndex) {
    case 25:  // {eoi}
       this.state = 0; 
      break;
    case 0:  // <default>
       this.quantifierReady(); 
      break;
    }
    return !(spaceToken);
  },

  quantifierReady: function() {
    if (this.chr == 0) {
      if (this.state == 1) this.state = 0;
      return;
    }
    if (this.state == 0) this.state = 1;
  },

  next: function() {
    var lapg_n = {};
    var state;

    do {
      lapg_n.offset = this.currOffset;
      this.tokenLine = lapg_n.line = this.currLine;
      lapg_n.column = this.currColumn;
      this.token = "";
      var tokenStart = this.offset - 1;

      for (state = this.tmStateMap[this.state]; state >= 0;) {
        state = this.tmGoto[state * this.tmClassesCount + this.mapCharacter(this.chr)];
        if (state == -1 && this.chr === 0) {
          lapg_n.endoffset = this.currOffset;
          lapg_n.endline = this.currLine;
          lapg_n.endcolumn = this.currColumn;
          lapg_n.symbol = 0;
          lapg_n.value = null;
          this.errorHandler("Unexpected end of input reached", lapg_n.line, lapg_n.offset, lapg_n.column, lapg_n.endline, lapg_n.endoffset, lapg_n.endcolumn);
          lapg_n.offset = this.currOffset;
          return lapg_n;
        }
        if (state >= -1 && this.chr !== 0) {
          this.currOffset++;
          this.currColumn++;
          if (this.chr == 10) {
            this.currColumn = 1;
            this.currLine++;
          }
          this.chr = this.offset < this.text.length ? this.text.charCodeAt(this.offset++) : 0;
        }
      }
      lapg_n.endoffset = this.currOffset;
      lapg_n.endline = this.currLine;
      lapg_n.endcolumn = this.currColumn;

      if (state == -2) {
        lapg_n.symbol = 0;
        lapg_n.value = null;
        return lapg_n;
      }

      this.token = this.text.slice(tokenStart, this.currOffset);
      if (state == -1) {
        this.errorHandler("invalid lexeme at line " + this.currLine + ": `" + this.token + "`, skipped", lapg_n.line, lapg_n.offset, lapg_n.column, lapg_n.endline, lapg_n.endoffset, lapg_n.endcolumn);
        lapg_n.symbol = -1;
        continue;
      }

      lapg_n.symbol = this.tmRuleSymbol[-state - 3];
      lapg_n.value = null;

    } while (lapg_n.symbol == -1 || !this.createToken(lapg_n, -state - 3));
    return lapg_n;
  }
};

jsregex.Nonterminals = {
  pattern: 22,
  part: 23,
  primitive_part: 24,
  setsymbol: 25,
  charset: 26,
  parts: 27,
  partsopt: 28
};

jsregex.Parser = function(errorHandler) {
  this.errorHandler = errorHandler;

  this.tmHead = 0;
  this.tmStack = [];
  this.tmNext = null;
  this.tmLexer = null;
};

jsregex.Parser.prototype = {
  tmAction: [
    -3, 9, 10, 11, 12, -25, -1, -1, 16, -1, 26, -47, -79, 2, -1, 17,
    18, 19, 20, 21, -1, -1, -103, 5, 6, 7, 8, 27, 13, 14, -127, 22,
    15, 3, 24, 25, -2
  ],
  tmLalr: [
    1, -1, 2, -1, 3, -1, 4, -1, 12, -1, 16, -1, 17, -1, 18, -1,
    0, 1, 13, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 12, -1,
    16, -1, 17, -1, 18, -1, 13, 1, 14, 1, -1, -2, 5, -1, 6, -1,
    7, -1, 8, -1, 0, 4, 1, 4, 2, 4, 3, 4, 4, 4, 12, 4,
    13, 4, 14, 4, 16, 4, 17, 4, 18, 4, -1, -2, 1, -1, 2, -1,
    3, -1, 4, -1, 12, -1, 16, -1, 17, -1, 18, -1, 0, 0, 13, 0,
    14, 0, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 12, -1, 16, -1,
    17, -1, 18, -1, 0, 1, 13, 1, 14, 1, -1, -2, 1, -1, 2, -1,
    3, 23, 20, 23, 21, 23, -1, -2
  ],
  lapg_sym_goto: [
    0, 1, 10, 19, 27, 31, 32, 33, 34, 35, 35, 35, 35, 39, 41, 42,
    42, 46, 50, 54, 54, 56, 60, 62, 66, 70, 74, 76, 79, 82
  ],
  lapg_sym_from: [
    9, 0, 5, 6, 7, 12, 20, 21, 22, 30, 0, 5, 6, 7, 12, 20,
    21, 22, 30, 0, 5, 6, 7, 12, 20, 21, 22, 0, 5, 12, 22, 11,
    11, 11, 11, 0, 5, 12, 22, 9, 14, 14, 0, 5, 12, 22, 0, 5,
    12, 22, 0, 5, 12, 22, 20, 21, 6, 7, 20, 21, 0, 5, 0, 5,
    12, 22, 0, 5, 12, 22, 6, 7, 20, 21, 6, 7, 0, 5, 22, 0,
    5, 22
  ],
  lapg_sym_to: [
    36, 1, 1, 15, 15, 1, 15, 15, 1, 34, 2, 2, 16, 16, 2, 16,
    16, 2, 35, 3, 3, 17, 17, 3, 17, 17, 3, 4, 4, 4, 4, 23,
    24, 25, 26, 5, 5, 5, 5, 22, 22, 28, 6, 6, 6, 6, 7, 7,
    7, 7, 8, 8, 8, 8, 29, 32, 18, 18, 30, 30, 9, 14, 10, 10,
    27, 10, 11, 11, 11, 11, 19, 19, 31, 31, 20, 21, 12, 12, 12, 13,
    13, 33
  ],
  tmRuleLen: [
    1, 0, 1, 3, 1, 2, 2, 2, 2, 1, 1, 1, 1, 3, 3, 3,
    1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 1, 2
  ],
  tmRuleSymbol: [
    28, 28, 22, 22, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24,
    24, 25, 25, 25, 26, 26, 26, 26, 26, 26, 27, 27
  ],
  tmSymbolNames: [
    "eoi",
    "char",
    "escaped",
    "charclass",
    "'.'",
    "'*'",
    "'+'",
    "'?'",
    "quantifier",
    "op_minus",
    "op_union",
    "op_intersect",
    "'('",
    "'|'",
    "')'",
    "'(?'",
    "'['",
    "'[^'",
    "expand",
    "kw_eoi",
    "']'",
    "'-'",
    "pattern",
    "part",
    "primitive_part",
    "setsymbol",
    "charset",
    "parts",
    "partsopt"
  ],

  /**
   * -3-n   Lookahead (state id)
   * -2     Error
   * -1     Shift
   * 0..n   Reduce (rule index)
   */
  action: function(state, symbol) {
    var p;
    if (this.tmAction[state] < -2) {
      for (p = -this.tmAction[state] - 3; this.tmLalr[p] >= 0; p += 2) {
        if (this.tmLalr[p] === symbol) {
          break;
        }
      }
      return this.tmLalr[p + 1];
    }
    return this.tmAction[state];
  },

  tmGoto: function(state, symbol) {
    var min = this.lapg_sym_goto[symbol], max = this.lapg_sym_goto[symbol + 1] - 1;
    var i, e;

    while (min <= max) {
      e = (min + max) >> 1;
      i = this.lapg_sym_from[e];
      if (i == state) {
        return this.lapg_sym_to[e];
      } else if (i < state) {
        min = e + 1;
      } else {
        max = e - 1;
      }
    }
    return -1;
  },

  parse: function(lexer) {
    this.tmLexer = lexer;
    this.tmStack = [];
    this.tmHead = 0;

    this.tmStack[0] = {state: 0};
    this.tmNext = lexer.next();

    while (this.tmStack[this.tmHead].state != 36) {
      var action = this.action(this.tmStack[this.tmHead].state, this.tmNext.symbol);

      if (action >= 0) {
        this.reduce(action);
      } else if (action == -1) {
        this.shift();
      }

      if (action == -2 || this.tmStack[this.tmHead].state == -1) {
        break;
      }
    }

    if (this.tmStack[this.tmHead].state != 36) {
      this.errorHandler("syntax error before line " + lexer.tokenLine + ", column " + this.tmNext.column, this.tmNext.line, this.tmNext.offset, this.tmNext.column, this.tmNext.endline, this.tmNext.endoffset, this.tmNext.endcolumn);
      throw new Error("syntax error");
    }
    return this.tmStack[this.tmHead - 1].value;
  },

  shift: function() {
    this.tmStack[++this.tmHead] = this.tmNext;
    this.tmStack[this.tmHead].state = this.tmGoto(this.tmStack[this.tmHead - 1].state, this.tmNext.symbol);
    if (jsregex.DEBUG_SYNTAX) {
      console.log("shift: " + this.tmSymbolNames[this.tmNext.symbol] + " (" + this.tmLexer.token + ")");
    }
    if (this.tmStack[this.tmHead].state != -1 && this.tmNext.symbol != 0) {
      this.tmNext = this.tmLexer.next();
    }
  },

  reduce: function(rule) {
    var tmLeft = {
      value: (this.tmRuleLen[rule] != 0) ? this.tmStack[this.tmHead + 1 - this.tmRuleLen[rule]].value : null,
      symbol: this.tmRuleSymbol[rule],
      state: 0
    };
    if (jsregex.DEBUG_SYNTAX) {
      console.log("reduce to " + this.tmSymbolNames[this.tmRuleSymbol[rule]]);
    }
    var startsym = (this.tmRuleLen[rule] != 0) ? this.tmStack[this.tmHead + 1 - this.tmRuleLen[rule]] : this.tmNext;
    tmLeft.line = startsym.line;
    tmLeft.column = startsym.column;
    tmLeft.offset = startsym.offset;
    tmLeft.endline = (this.tmRuleLen[rule] != 0) ? this.tmStack[this.tmHead].endline : this.tmNext.line;
    tmLeft.endcolumn = (this.tmRuleLen[rule] != 0) ? this.tmStack[this.tmHead].endcolumn : this.tmNext.column;
    tmLeft.endoffset = (this.tmRuleLen[rule] != 0) ? this.tmStack[this.tmHead].endoffset : this.tmNext.offset;
    this.applyRule(tmLeft, rule, this.tmRuleLen[rule]);
    for (var e = this.tmRuleLen[rule]; e > 0; e--) {
      this.cleanup(this.tmStack[this.tmHead]);
      this.tmStack[this.tmHead--] = null;
    }
    this.tmStack[++this.tmHead] = tmLeft;
    this.tmStack[this.tmHead].state = this.tmGoto(this.tmStack[this.tmHead - 1].state, tmLeft.symbol);
  },

  /**
   * cleans node removed from the stack
   */
  cleanup: function(value) {
  },

  report: function(start, end, text) {
    this.entities.push({start: start, end: end, text: text});
  },

  applyRule: function(tmLeft, tmRule, tmLength) {
    switch (tmRule) {
    case 3:  // pattern ::= pattern '|' partsopt
       this.report(tmLeft.offset, tmLeft.endoffset, "or"); 
      break;
    case 5:  // part ::= primitive_part '*'
       this.report(tmLeft.offset, tmLeft.endoffset, "*"); 
      break;
    case 6:  // part ::= primitive_part '+'
       this.report(tmLeft.offset, tmLeft.endoffset, "+"); 
      break;
    case 8:  // part ::= primitive_part quantifier
       this.report(tmLeft.offset, tmLeft.endoffset, "{,}"); 
      break;
    case 10:  // primitive_part ::= escaped
       this.report(tmLeft.offset, tmLeft.endoffset, "escaped"); 
      break;
    case 13:  // primitive_part ::= '(' pattern ')'
       this.report(tmLeft.offset, tmLeft.endoffset, "()"); 
      break;
    case 14:  // primitive_part ::= '[' charset ']'
       this.report(tmLeft.offset, tmLeft.endoffset, "charset"); 
      break;
    }
  }
};

root.jsregex = jsregex;

}).call(this);
