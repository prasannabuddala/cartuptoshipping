package BLLs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DALs.GetProductsDAL;
import Modals.CartItem;
import Modals.Products;

public class CalculateChargesBLL {
	Map<Integer, Double> gstOnEachProduct = new HashMap<>();
	Map<Integer, Double> productWiseShippingGst = new HashMap<>();

	double prodOrderTotal = 0;
	double totalGstOnAllProducts = 0;
	double totalGstOnShipping = 0;

	public double getOrderTotal(List<CartItem> cartitems) {
		for (CartItem item : cartitems) {
			Products p = new GetProductsDAL().getProductById(item.getPid());
			double prodTotal = item.getQuantity() * p.getPprice();
			prodOrderTotal += prodTotal;

			double gstPerProduct = new GetProductsDAL().getGstForProduct(item.getPid());
			double gstAmount = (p.getPprice() - (calculateOriginalPrice(p.getPprice(), gstPerProduct)));
			double roundedGst = Math.round(gstAmount * Math.pow(10, 2)) / Math.pow(10, 2);
			double totalGstOnEachProduct = roundedGst * item.getQuantity();
			totalGstOnAllProducts += totalGstOnEachProduct;

			gstOnEachProduct.put(item.getPid(), totalGstOnEachProduct);
		}
		return prodOrderTotal;
	}

	public double calculateOriginalPrice(double priceAfterGST, double gstPercent) {
		return priceAfterGST / (1 + (gstPercent / 100));
	}

	public Map<Integer, Double> getProductWiseGsts() {
		return gstOnEachProduct;
	}

	public double getGstOnAllProducts() {
		return totalGstOnAllProducts;
	}

	public double getShippingCharge(double tot) {
		double ship = new GetProductsDAL().getShippingAmount(tot);
		return ship;
	}

	public double getTotalShippingPrice(double shippingCharge, List<CartItem> cartItems) {
		double totalShippingCharge = 0;
		for (CartItem item : cartItems) {
			double proportion = (item.getQuantity() * (new GetProductsDAL().getProductById(item.getPid()).getPprice()))
					/ prodOrderTotal;
			double allocatedShippingCharge = shippingCharge * proportion;
			double gstPerProduct = gstOnEachProduct.get(item.getPid());
			double gstOnShipping = (allocatedShippingCharge * gstPerProduct) / 100;
			productWiseShippingGst.put(item.getPid(), gstOnShipping);
			totalGstOnShipping += gstOnShipping;
			totalShippingCharge += allocatedShippingCharge + gstOnShipping;
		}
		return totalShippingCharge;
	}

	public double getTotalGstOnShipping() {
		return totalGstOnShipping;
	}

	public Map<Integer, Double> getProductWiseShiipingGst() {
		return productWiseShippingGst;
	}

}

// System.out.print("prod id: " + item.getPid() + " product price after gst: " + p.getPprice()
// + " price before gst: " + calculateOriginalPrice(p.getPprice(), gstPerProduct) + " gst amount is: "
// + (p.getPprice() - (calculateOriginalPrice(p.getPprice(), gstPerProduct))));
// System.out.println("gst amount for " + p.getPprice() + " is: " + gstAmount + "..." + roundedGst);