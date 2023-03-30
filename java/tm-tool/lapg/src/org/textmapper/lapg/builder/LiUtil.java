/**
 * Copyright 2002-2022 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.lapg.common.FormatUtil;

public class LiUtil {

	public static String getSymbolName(RhsSymbol s) {
		TemplateParameter templateTarget = s.getTemplateTarget();
		if (templateTarget != null) {
			return templateTarget.getNameText();
		}

		return getSymbolName(s.getTarget());
	}

	public static String getSymbolName(Symbol s) {
		String name = s.getNameText();
		if (name != null) return name;

		// for anonymous nonterminals we can get an approximate name from nameHint user data.
		return s.getNameHint();
	}

	public static String getSymbolID(RhsSymbol s) {
		TemplateParameter templateTarget = s.getTemplateTarget();
		if (templateTarget != null) {
			return templateTarget.getNameText();
		}

		return getSymbolID(s.getTarget());
	}

	public static String getSymbolID(Symbol s) {
		Name name = s.getName();
		if (name != null) {
			String text = name.text();
			if (!FormatUtil.isIdentifier(text)) {
				if (s.isTerm() && ((Terminal)s).isConstant()) {
					text = ((Terminal) s).getConstantValue();
				}
				text = FormatUtil.toIdentifier(text);
			}
			if (s.isTerm()) {
				text = text.toUpperCase();
			}
			return text;
		}

		// for anonymous nonterminals we can get an approximate name from nameHint user data.
		return s.getNameHint();
	}

	public static void appendArguments(StringBuilder sb, LiRhsArgument[] args) {
		if (args == null || args.length == 0) return;

		sb.append("<");
		boolean first = true;
		for (LiRhsArgument arg : args) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			arg.toString(sb);
		}
		sb.append(">");
	}
}
