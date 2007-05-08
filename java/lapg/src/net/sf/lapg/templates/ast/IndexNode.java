package net.sf.lapg.templates.ast;


public class IndexNode extends ExpressionNode {

	ExpressionNode object;
	Object index;

	public IndexNode(ExpressionNode object, Object index) {
		this.object = object;
		this.index = index;
	}

	public Object resolve(Object context) {
		Object objContext = object.resolve(context);
		if( objContext == null )
			return null;

		if( objContext instanceof Object[]) {
			Object i = index;
			if( index instanceof ExpressionNode )
				i = ((ExpressionNode)index).resolve(context);
			
			if( i instanceof Integer )
				return ((Object[])objContext)[(Integer)index];
			else
				; // fire error
		}
		return null;
	}
}
