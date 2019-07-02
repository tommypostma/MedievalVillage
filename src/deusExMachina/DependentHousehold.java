package deusExMachina;

public class DependentHousehold extends MedievalEconomicActor {
	// to have permanently the number of serf households, we use a static
	// variable in this class
	// this variable, named houses, will be increased when a new serf household
	// is created
	private static int houses = 0;
	// and it will be decreased when the household "dies" (it decreases to zero
	// adults, in case you implement demographics)

	// the households has assets, like any household, but now they are inherited
	// from the superclass, the same for the adults attribute

	private double currentCorveePerWeekPerAdult;
	// each household may have a different level of obligation, which may also
	// change

	public final int houseNo;
	// it is useful to have a "house number" in the village, when we display
	// information about this household. After this value is created, it cannot
	// be changed (thus is can be left public).

	// the constructor sets the number of adults, the house number, and some
	// asset levels
	private int yearsInSerfdom = 0;
	private double totalAgriProduceSold = 0;

	public DependentHousehold(int adults) {

		// we changed also a bit the code due to the extant code in the
		// superclass
		super();
		super.setTheNumberOfAdults(adults);

		/*
		 * for (AssetList asset : AssetList.values()) { assets[asset.ordinal()]
		 * = 0; } this.adults = adults;
		 */

		houses++;// the static variable is increased from the previously created
					// household
		this.houseNo = houses;// this value is used to give a unique number to
								// the house
		currentCorveePerWeekPerAdult = ExP.typicalCorveeHoursPerWeekPerAdult;
		assets[AssetList.SILVER.ordinal()] = ExP.typicalSilverQuantityPerSerfHousehold;
		assets[AssetList.TOWN_PRODUCT.ordinal()] = ExP.typicalGoodsQuantityPerSerf * adults;
	}

	// first, a serf household is bound to a piece of land
	public void boundTo(double parcelForSerf) {
		assets[AssetList.RESIDENCE_LAND.ordinal()] += parcelForSerf;
	}

	// a simple method to report
	public void reportAdultsAndBoundLandSurface() {
		System.out.println("serf house no " + this.houseNo + " has " + this.howManyAdults()
				+ " adults, and is bound to a surface of " + assets[AssetList.RESIDENCE_LAND.ordinal()]);
	}

	// this method computes how much a serf household can work in the whole year
	// and how much will be given to the nobleman (according to the regulator,
	// of course)
	public void computeYearlyLaborPotentialBy(LocalChurch regulator) {
		// first, the maximum number of hours per year is calculated
		double total = assets[AssetList.LABOR.ordinal()] = this.howManyAdults()
				* regulator.regulatedWorkingWeekHoursForSerfs() * ExP.workingWeeksInAYear;
		// after that, the regulator decides how much can be given to the
		// nobleman
		currentCorveePerWeekPerAdult = regulator.regulatesCorvee(total, assets[AssetList.OBLIGATION.ordinal()],
				this.howManyAdults());
		// the number of corvée hours remaining from the previous year is to be
		// added to the total
		// or if the regulator decides, to be ignored - it is up to them to set
		// the formula
	}

	// this method is always necessary to end a transaction
	public void acceptsPartOrWholeOf(Exchange offer) {
		double toAccept;
		switch (offer.getWhatIsGiven()) {
		case OBLIGATION:
			toAccept = currentCorveePerWeekPerAdult * this.howManyAdults() * ExP.workingWeeksInAYear;
			break;
		case TOWN_PRODUCT:
			switch (offer.getWhatIsTaken()) {
			case SILVER:         
				toAccept = acceptAsMuchAsINeed(offer, ExP.serfHouseholdSilverReserve, ExP.typicalGoodsQuantityPerSerf,
						ExP.serfHouseholdSafetyLevel, ExP.serfHouseholdRestockLevel);
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
		case RESIDENCE_LAND:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				// getting rid of bondage (giving away the residence land)
				toAccept = -assets[AssetList.RESIDENCE_LAND.ordinal()];
				// and now becoming true owner of land and FREE!
				assets[AssetList.DIRECTLY_OWNED_LAND.ordinal()] += (-toAccept);
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
		// the offer is settled after the quantity to be given is computed above
		offer.setQuantityGiven(toAccept);
		offer.setQuantityTaken(toAccept * offer.getPricePerUnit());
		// the exchange rate (price) was set by the regulator
		// and now, the asset values are set according with what was given and
		// taken
		assets[offer.getWhatIsGiven().ordinal()] += toAccept;
		assets[offer.getWhatIsTaken().ordinal()] -= toAccept * offer.getPricePerUnit();
	}

	// this method is necessary to initiate any transaction
	public Exchange offers(AssetList what, AssetList whatFor) {
		Exchange offer = new Exchange(what, whatFor);
		double offeredQuantity;
		switch (what) {
		case LABOR:// labor is sold against obligation to protect
			// some randomness in the value is introduced via the static method
			// from ExP
			offeredQuantity = currentCorveePerWeekPerAdult * this.howManyAdults()
					+ ExP.corveeVariability(currentCorveePerWeekPerAdult * this.howManyAdults());
			break;
		case AGRI_PRODUCE:// the serf offers all the surplus food he has after
							// consumption
			// normally, he should be careful and keep a bit, just in case
			offeredQuantity = assets[AssetList.AGRI_PRODUCE.ordinal()]; //- 8;
//			if (offeredQuantity < 0) {
//				offeredQuantity = 0;
//			}
			break;
		default:
			offeredQuantity = 0;
			break;
		}
		offer.setQuantityGiven(offeredQuantity);
		return offer;
	}

	// like in the Manor House class, the end of the transaction is done by
	// changing the assets
	// of the party that receives something in exchange for what was initially
	// offered
	public void settlesTransactionOver(Exchange exchange) {
		assets[exchange.getWhatIsGiven().ordinal()] -= exchange.getQuantityGiven();
		if (exchange.getWhatIsGiven() == AssetList.AGRI_PRODUCE) {// to sum up
																	// for R
			this.totalAgriProduceSold += exchange.getQuantityGiven();
		}
		assets[exchange.getWhatIsTaken().ordinal()] += exchange.getQuantityTaken();
	}

	// we use the same productionIf() method from the ExP class
	public void produces(int currentWeekNo) {
		// first, calculate how much labor hours to invest, dependent on where
		// we are in the year
		double laborThisWeek = assets[AssetList.LABOR.ordinal()] / (ExP.workingWeeksInAYear - currentWeekNo + 2);
		assets[AssetList.AGRI_PRODUCE.ordinal()] += ExP.productionIf(assets[AssetList.RESIDENCE_LAND.ordinal()],
				laborThisWeek);
		// and then we update the value of the labor left
		assets[AssetList.LABOR.ordinal()] -= laborThisWeek;
	}

	// this method is very similar to the consumes() method of the Manor house
	// but of course, the weekly ration per adult is different
	public void consumes() {
		// first, we have to calculate the whole household consumption
		double consumption = ExP.weeklyRationForSerfs * this.howManyAdults();
		if (consumption > assets[AssetList.AGRI_PRODUCE.ordinal()]) {
			assets[AssetList.AGRI_PRODUCE.ordinal()] = 0;
		} else {
			assets[AssetList.AGRI_PRODUCE.ordinal()] -= consumption;
		}
		// again, we could register shortages and compute a degree of famine
		// if this degree is too high, the number of adults can shrink, because
		// they die of malnutrition
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

	public boolean canBuyItsFreedomAccordingTo(LocalChurch priest) {
		yearsInSerfdom++;
		/*
		 * each year the serf asks the church, he counts the years of live in
		 * servitude
		 */
		return /* a boolean answer from a */priest.isAskedIfFreedomIsPossibleFor(this /* serf */);
	}

	public FreeHousehold becomesFree() {
		FreeHousehold fromNowOn = new FreeHousehold(this.houseNo);
		fromNowOn.setTheNumberOfAdults(this.howManyAdults());
		fromNowOn.rememberDurationOf(this.yearsInSerfdom);
		fromNowOn.rememberThe(this.totalAgriProduceSold);
		int assetCounter = 0;
		for (double asset : assets) {
			fromNowOn.assets[assetCounter] = asset;
			assetCounter++;
		}
		return fromNowOn;
	}

	// you need here some getters for the silver levels, adults, land
	// area, etc, to be able to write in the csv file
	// like this ones
	public int getFamilySize() {
		return this.howManyAdults();
	}

	public boolean hasOwnLand() {
		return assets[AssetList.DIRECTLY_OWNED_LAND.ordinal()] > 0;
	}

	public double getResidenceLandArea() {
		return assets[AssetList.RESIDENCE_LAND.ordinal()];
	}

	public double getSilverReserve() {
		return assets[AssetList.SILVER.ordinal()];
	}

	// to display in the console
	public void reportsSilver() {
		System.out.println(
				"serf house no " + houseNo + " has a quantity of " + assets[AssetList.SILVER.ordinal()] + " silver");
	}

	public void reportsGoods() {
		System.out.println("serf house no " + houseNo + " has a quantity of " + assets[AssetList.TOWN_PRODUCT.ordinal()]
				+ " goods");
	}

	public void reportsFood() {
		System.out.println("serf house no " + houseNo + " has a quantity of " + assets[AssetList.AGRI_PRODUCE.ordinal()]
				+ " foods");
	}
}
