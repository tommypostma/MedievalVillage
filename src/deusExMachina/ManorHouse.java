package deusExMachina;

public class ManorHouse extends MedievalEconomicActor {
	// like everybody else, the ManorHouse has stocks of various assets
	// but like the adults attribute, they are not inherited from the superclass
	/*
	 * 
	 * the constructor of the ManorHouse objects sets the number of adults in
	 * the households and the values of some of the assets (arable land, silver,
	 * and manufactured products)
	 */
	public ManorHouse(double ownedLandSurface, int adults) {
		// this time we have a superclass, and we code accordingly
		super();
		super.setTheNumberOfAdults(adults);

		assets[AssetList.DIRECTLY_OWNED_LAND.ordinal()] = ownedLandSurface;
		assets[AssetList.SILVER.ordinal()] = ExP.initialSilverValueForManorHouse;
		assets[AssetList.TOWN_PRODUCT.ordinal()] = ExP.initialTownProductStockLevelForManorHouse;
		// This time, we initialize the values on more realistic values
	}

	// the nobleman can assign the parcel for each serf
	public void allocatesLandForResidence(DependentHousehold serf) {
		// to create the parcel, we use a static method from the ExP class
		double surfaceGivenForResidence = ExP.createParcelForSerf();
		// the surface is communicated to the serf, and it becomes part of its
		// assets
		serf.boundTo(surfaceGivenForResidence);
		// and the land in direct ownership of the nobleman is reduced
		// accordingly
		assets[AssetList.DIRECTLY_OWNED_LAND.ordinal()] -= surfaceGivenForResidence;
	}

	// the offers method is creating an Exchange object, which is used to
	// initiate a transaction
	public Exchange offers(AssetList what, AssetList whatFor) {
		Exchange offer = new Exchange(what, whatFor);
		double offeredQuantity;
		switch (what) {
		case OBLIGATION: // in case of "offering" an obligation to protect
			// the nobleman expects "everything" in return, therefore we set the
			// value to a maximum
			offeredQuantity = Integer.MAX_VALUE;
			break;
		case TOWN_PRODUCT:
			switch (whatFor) {
			case SILVER:
				offeredQuantity = goodsForSaleToSerfs();
				/*
				 * This time, he offers only a part of his town produce stocks
				 * to the serfs, and only in case he has enough safety stock,
				 * see the method just below this one
				 */
				break;
			default:
				offeredQuantity = 0;
				break;
			}
			break;
		case AGRI_PRODUCE:
			switch (whatFor) {
			case SILVER:
				offeredQuantity = assets[AssetList.AGRI_PRODUCE.ordinal()] / 2;
				/*
				 * offers half of his food reserves to the town merchants, and
				 * keeps half in case of famine
				 */
				break;
			default:
				offeredQuantity = 0;
				break;
			}
			break;
		case RESIDENCE_LAND:
			// nothing to offer really, the exchange will take place within the
			// serf household object
		default:
			offeredQuantity = 0;
			break;	
		}
		offer.setQuantityGiven(offeredQuantity);
		return offer;
	}

	private double goodsForSaleToSerfs() {
		double toSellToASerf = 0;
		if (assets[AssetList.TOWN_PRODUCT.ordinal()] > (0.5 * ExP.initialTownProductStockLevelForManorHouse)) {
			toSellToASerf = assets[AssetList.TOWN_PRODUCT.ordinal()] * 0.2;
		}
		return toSellToASerf;
	}

	// this method is necessary to finish any transaction, by changing the
	// values in the assets array
	public void settlesTransactionOver(Exchange exchange) {
		assets[exchange.getWhatIsGiven().ordinal()] -= exchange.getQuantityGiven();
		assets[exchange.getWhatIsTaken().ordinal()] += exchange.getQuantityTaken();

	}

	// this method is necessary to accept a transaction offer
	public void acceptsPartOrWholeOf(Exchange offer) {
		double toAccept;
		switch (offer.getWhatIsGiven()) {
		case LABOR:// all corvee labor has to be accepted by the nobleman
			toAccept = offer.getQuantityGiven();
			break;
		case AGRI_PRODUCE:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				toAccept = acceptAsMuchAsICanPay(offer, ExP.manorHouseSilverReserve);
				/*
				 * this time the silver levels are checked, otherwise they
				 * become negative
				 */
				break;
			default:
				toAccept = 0;
				break;
			}
			break;
		case TOWN_PRODUCT:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				toAccept = acceptAsMuchAsINeed(offer, ExP.manorHouseSilverReserve,
						ExP.initialTownProductStockLevelForManorHouse, ExP.manorHouseSafetyLevel,
						ExP.manorHouseRestockLevel);
				/*
				 * the method invocation above is necessary to establish a
				 * cautious behaviour, and spend only part of the silver on
				 * manufactured goods
				 */
				break;
			default:
				toAccept = 0;
				break;
			}
			break;
		default:// the outer switch default case
			toAccept = 0;
			break;
		}// the outer switch ends here

		// the offer is settled after the quantity to be given is computed above
		offer.setQuantityGiven(toAccept);
		offer.setQuantityTaken(toAccept * offer.getPricePerUnit());
		// the exchange rate (price) was set by the regulator
		// and now, the asset values are set according with what was given and
		// taken
		assets[offer.getWhatIsGiven().ordinal()] += toAccept;
		assets[offer.getWhatIsTaken().ordinal()] -= toAccept * offer.getPricePerUnit();
		// the last part of the code is identical (symmetrical) with the one for
		// the serf household
	}

	// the serfs gave their labor to the nobleman and this is used on its own
	// land (demesne) to generate agricultural produce that is owned directly by
	// the nobleman
	public void produces() {
		// we use here the static method productionIf() is from the experiment
		// parameters class ExP. getDemesne is a method of the ManorHouse class
		assets[AssetList.AGRI_PRODUCE.ordinal()] += ExP.productionIf(this.getDemesne(),
				assets[AssetList.LABOR.ordinal()]);
		assets[AssetList.LABOR.ordinal()] = 0;
	}

	// this method does not used randomization - everybody eats the same ration
	// each week
	public void consumes() {
		// first, we have to calculate the whole household consumption
		double consumption = ExP.weeklyRationManorPeople * this.howManyAdults();
		if (consumption > assets[AssetList.AGRI_PRODUCE.ordinal()]) {
			assets[AssetList.AGRI_PRODUCE.ordinal()] = 0;
		} else {
			assets[AssetList.AGRI_PRODUCE.ordinal()] -= consumption;
		}
		// in this way, the food stock cannot be negative
		// here, we could register shortages and compute a degree of famine
		// if this degree is too high, the number of adults can shrink, because
		// they die of malnutrition
		// or, we can imagine that the people in the manor house will just buy
		// food from the serf
		// because they have plenty of silver
	}

	// and we make a method that make sure that the manufactured good depletion
	// is not yielding negative stock levels
	public void depleteGoodsWithTheSameAmountAs() {
		double depletion = this.howManyAdults() * ExP.goodsDepletionRate;
		if (depletion > assets[AssetList.TOWN_PRODUCT.ordinal()]) {
			assets[AssetList.TOWN_PRODUCT.ordinal()] = 0;
		} else {
			assets[AssetList.TOWN_PRODUCT.ordinal()] -= depletion;
		}
	}

	// a not so necessary getter method, but it makes the code of produces()
	// simpler to understand
	public double getDemesne() {
		return assets[AssetList.DIRECTLY_OWNED_LAND.ordinal()];
	}

	// to be able to visualize how rich or poor they got...
	public void reportsSilver() {
		System.out.println("The Manor house has a quantity of " + assets[AssetList.SILVER.ordinal()] + " silver");
		if (assets[AssetList.SILVER.ordinal()] < 0) {
			System.err.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ NEGATIVE SILVER LEVEL AT MANOR!!!!!!!!!");
		}
	}

	// to make sure that the goods' stock never get negative, and...
	public void reportsGoods() {
		System.out.println("Manor house has a quantity of " + assets[AssetList.TOWN_PRODUCT.ordinal()] + " goods");
		if (assets[AssetList.TOWN_PRODUCT.ordinal()] < 0) {
			System.err.println("#################################### NEGATIVE GOOD LEVEL AT MANOR!!!!!!!!!");
		}

	}

	// ...same for the foods
	public void reportsFood() {
		System.out.println("Manor house has a quantity of " + assets[AssetList.AGRI_PRODUCE.ordinal()] + " foods");
		if (assets[AssetList.AGRI_PRODUCE.ordinal()] < 0) {
			System.err.println("#################################### NEGATIVE FOOD LEVEL AT MANOR!!!!!!!!!");
		}
	}

}
