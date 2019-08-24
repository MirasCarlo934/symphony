package bm.cir.objects;

public enum ArgOperator {
	EQUALS("="), LESS("<"), GREATER(">"), LESSEQUALS("<="), GREATEREQUALS(">="), INEQUAL("!=");
	
	private String symbol;
	
	ArgOperator(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}
	
	/**
	 * Translates the operator symbols into an ArgOperator. The only acceptable symbols are <b>=, >, <, >=, <=, !=</b>
	 * @param symbol
	 * @return
	 */
	public static ArgOperator translate(String symbol) throws IllegalArgumentException{
		ArgOperator operator = null;
		if(symbol.equals("="))
			operator = ArgOperator.EQUALS;
		else if(symbol.equals("<"))
			operator = ArgOperator.LESS;
		else if(symbol.equals(">"))
			operator = ArgOperator.GREATER;
		else if(symbol.equals("<="))
			operator = ArgOperator.LESSEQUALS;
		else if(symbol.equals(">="))
			operator = ArgOperator.GREATEREQUALS;
		else if(symbol.equals("!="))
			operator = ArgOperator.INEQUAL;
		else
			throw new IllegalArgumentException("Invalid symbol");
		
		return operator;
	}
}
