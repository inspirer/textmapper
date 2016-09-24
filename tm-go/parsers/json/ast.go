package json

type Value interface {
	valueType()
}

type Literal struct {
	value string
}

func (literal Literal) valueType() {}

type Field struct {
	name string
}
