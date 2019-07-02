package deusExMachina;

public class ExternalTradingPartner extends MedievalEconomicActor {

	/*
	 * the assets array is now inherited from the superclass (where it is
	 * defined as protected, visible for inheritance for all subclasses)
	 */

	public ExternalTradingPartner() {
		super();// to set all stocks first on 0
		assets[AssetList.SILVER.ordinal()] = Integer.MAX_VALUE;
		assets[AssetList.TOWN_PRODUCT.ordinal()] = Integer.MAX_VALUE;
	}

	public Exchange offers(AssetList what, AssetList whatFor) {
		Exchange offer = new Exchange(what, whatFor);
		double offeredQuantity;
		switch (what) {
		case TOWN_PRODUCT:
			offeredQuantity = assets[AssetList.TOWN_PRODUCT.ordinal()];
			/*
			 * offers everything each time, because their reserves are still near
			 * infinite in this simulation.
			 */
			break;
		default:
			offeredQuantity = 0;
			break;
		}
		offer.setQuantityGiven(offeredQuantity);
		return offer;
	}

	public void acceptsPartOrWholeOf(Exchange offer) {
		double toAccept;
		switch (offer.getWhatIsGiven()) {
		case AGRI_PRODUCE:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				toAccept = acceptAsMuchAsICanPay(offer, ExP.townMerchantsSilverReserve);
				/*
				 * all the produce has to be accepted, silver reserves are near
				 * infinite
				 */
				break;
			default:
				toAccept = 0;
				break;
			}
			break;
		default:
			toAccept = 0;
			break;
		}
		offer.setQuantityGiven(toAccept);
		offer.setQuantityTaken(toAccept * offer.getPricePerUnit());
		// change your assets assets
		assets[offer.getWhatIsGiven().ordinal()] += toAccept;
		assets[offer.getWhatIsTaken().ordinal()] -= toAccept * offer.getPricePerUnit();

	}

	public void settlesTransactionOver(Exchange exchange) {
		assets[exchange.getWhatIsGiven().ordinal()] -= exchange.getQuantityGiven();
		assets[exchange.getWhatIsTaken().ordinal()] += exchange.getQuantityTaken();
	}

	/*
	 * the next three methods wee need because we may be need to report stocks
	 * and they have to be here anyway, due to the fact that they are inherited
	 * from the superclass MedievalEconomicActor
	 */
	public void reportsFood() {
		System.out.println("TOWN MERCHANTS have now " + assets[AssetList.AGRI_PRODUCE.ordinal()] + " foods");

	}

	// even if this is actually not necessary, because the town goods are very
	// highly set
	public void reportsGoods() {
		System.out.println("TOWN MERCHANTS have now " + assets[AssetList.TOWN_PRODUCT.ordinal()] + " goods");
	}

	// same for silver
	public void reportsSilver() {
		System.out.println("TOWN MERCHANTS have now " + assets[AssetList.SILVER.ordinal()] + " silver");

	}
}
