package js

import (
	"regexp"
	"testing"
)

func TestTokenRanges(t *testing.T) {
	keywordRE := regexp.MustCompile("^[a-z]+$")
	punctRE := regexp.MustCompile("^[^a-zA-Z\x00-\x1f]+$")
	for tok := EOI; tok < terminalEnd; tok++ {
		val := tok.String()
		if keywordRE.MatchString(val) != (tok >= keywordStart && tok < keywordEnd) {
			t.Errorf("All keywords must be in the range [keywordStart, keywordEnd): %d, %s", tok, val)
		}
		if punctRE.MatchString(val) != (tok >= punctuationStart && tok < punctuationEnd) {
			t.Errorf("All punctuation tokens must be in the range [punctuationStart, punctuationEnd): %d, %s", tok, val)
		}
	}
}

func TestStateValues(t *testing.T) {
	if StateDiv&^1 != StateInitial || StateJsxTemplateDiv&^1 != StateJsxTemplate || StateTemplateDiv&^1 != StateTemplate {
		t.Error("div states must be odd and one greater than non-div states")
	}
}
