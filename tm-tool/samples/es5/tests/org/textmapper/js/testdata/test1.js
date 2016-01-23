"strict"

var a = function() {}

a = function b(param) {
	return 0;
}

a = 5
{
	function q() {}

	(function(){
		a = 5; q()

		if (a) return 1;
		else if (q()) return 2
		else return 4;
	})();
}
