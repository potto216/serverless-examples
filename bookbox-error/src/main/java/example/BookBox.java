package example;

// There are problems with this code.

public class BookBox {
	private int totalBooks;
	private double pricePerBox;

	public BookBox(int totalBooks, double pricePerBox) {
		this.totalBooks = totalBooks;
		this.pricePerBox = pricePerBox;
	}

	public double calcAvgPricePerBook() {
		int bookPrice = (int) (pricePerBox);
		bookPrice = bookPrice / totalBooks;

		return bookPrice;
	}

}