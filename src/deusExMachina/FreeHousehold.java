package deusExMachina;

public class FreeHousehold extends MedievalEconomicActor {

	private int houseNo; // it will be the same as the initial serf house number
	private int serfdomPeriod;

	private double totalPreviousFoodContributionAsSerfHousehold;

	public FreeHousehold(int house) {
		super();
		houseNo = house;
	}

	public void produces(int currentWeekNo) {
		double laborThisWeek = assets[AssetList.LABOR.ordinal()] / (ExP.workingWeeksInAYear - currentWeekNo + 2);
		assets[AssetList.AGRI_PRODUCE.ordinal()] += ExP.productionIf(getOwnedLand(), laborThisWeek);
		assets[AssetList.LABOR.ordinal()] -= laborThisWeek;
	}

	// computing how much a household can work in a year, none of it is corvée
	public void computeYearlyLaborPotentialBy(LocalChurch regulator) {
		assets[AssetList.LABOR.ordinal()] = howManyAdults() * regulator.regulatedWorkingWeekHoursForSerfs()
				* ExP.workingWeeksInAYear;
	}

	public void consumes() {
		double consumption = (ExP.weeklyRationForSerfs + ExP.weeklyRationManorPeople) / 2 * howManyAdults();
		if (consumption > getStockOf(AssetList.AGRI_PRODUCE)) {
			assets[AssetList.AGRI_PRODUCE.ordinal()] = 0;
		} else {
			assets[AssetList.AGRI_PRODUCE.ordinal()] -= consumption;
		}
	}

	public void depleteGoodsWithTheSameAmountAs() {
		double depletion = howManyAdults() * ExP.goodsDepletionRate;
		if (depletion > assets[AssetList.TOWN_PRODUCT.ordinal()]) {
			assets[AssetList.TOWN_PRODUCT.ordinal()] = 0;
		} else {
			assets[AssetList.TOWN_PRODUCT.ordinal()] -= depletion;
		}
	}

	public void rememberDurationOf(int serfdomPeriod) {
		this.serfdomPeriod = serfdomPeriod;
	}

	public void rememberThe(double totalPreviousFoodContributionAsSerfHousehold) {
		this.totalPreviousFoodContributionAsSerfHousehold = totalPreviousFoodContributionAsSerfHousehold;
	}

	@Override
	public Exchange offers(AssetList what, AssetList whatFor) {
		Exchange offer = new Exchange(what, whatFor);
		double offeredQuantity;
		switch (what) {
		case AGRI_PRODUCE:
			switch (whatFor) {
			case SILVER:
				// should keep some stock, just in case
				offeredQuantity = getStockOf(AssetList.AGRI_PRODUCE) * 0.9;
				break;
			default:
				offeredQuantity = 0;
				break;
			}
			break;
		default:
			offeredQuantity = 0;
			break;
		}
		offer.setQuantityGiven(offeredQuantity);
		return offer;
	}

	@Override
	public void acceptsPartOrWholeOf(Exchange offer) {
		double toAccept;
		switch (offer.getWhatIsGiven()) {
		case TOWN_PRODUCT:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				toAccept = acceptAsMuchAsINeed(offer, ExP.serfHouseholdSilverReserve,
						ExP.typicalGoodsQuantityForYeoman, ExP.serfHouseholdSafetyLevel,
						ExP.serfHouseholdRestockLevel);
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

	@Override
	public void settlesTransactionOver(Exchange exchange) {
		assets[exchange.getWhatIsGiven().ordinal()] -= exchange.getQuantityGiven();
		assets[exchange.getWhatIsTaken().ordinal()] += exchange.getQuantityTaken();

	}

	@Override
	public void reportsGoods() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportsSilver() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportsFood() {
		// TODO Auto-generated method stub

	}

	public double getOwnedLand() {
		return assets[AssetList.DIRECTLY_OWNED_LAND.ordinal()];
	}

	public int getHouseNo() {
		return houseNo;
	}

	public int getSerfdomPeriod() {
		return serfdomPeriod;
	}

	public double getTotalPreviousFoodContributionAsSerfHousehold() {
		return totalPreviousFoodContributionAsSerfHousehold;
	}

}
