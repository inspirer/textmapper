package tm

func IsSoftKeyword(t Token) bool {
	return t >= ASSERT && t <= CHAR_X
}

func IsKeyword(t Token) bool {
	return t >= AS && t <= TRUE
}