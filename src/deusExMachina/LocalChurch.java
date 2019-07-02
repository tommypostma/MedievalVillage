package deusExMachina;

public class LocalChurch extends ExchangeRegulator {

	private double workingWeekForSerf;
	private double corveeFraction;

	public LocalChurch(double exchangeRateTownProducts, double exchangeRateObligations, double exchangeRateAgriProduce,
			double exchangeRateLaborForObligations, double currentPriceOfLandPerAcre, double corveeFraction,
			double workingWeekForSerf) {
		super(exchangeRateTownProducts, exchangeRateAgriProduce);
		exchangeRates[AssetList.OBLIGATION.ordinal()] = exchangeRateObligations;
		exchangeRates[AssetList.LABOR.ordinal()] = exchangeRateLaborForObligations;
		exchangeRates[AssetList.RESIDENCE_LAND.ordinal()] = currentPriceOfLandPerAcre;
		this.workingWeekForSerf = workingWeekForSerf;
		this.corveeFraction = corveeFraction;
	}

	public double regulatedWorkingWeekHoursForSerfs() {
		return workingWeekForSerf;// this can change from year to year
	}

	public double regulatesCorvee(double labor, double obligation, int persons) {
		return corveeFraction * (labor + obligation) / ExP.workingWeeksInAYear / persons;
	}

	public boolean isAskedIfFreedomIsPossibleFor(DependentHousehold serf) {
		double priceOfFreedom = exchangeRates[AssetList.RESIDENCE_LAND.ordinal()] * serf.getResidenceLandArea();
		if (priceOfFreedom <= serf.getSilverReserve()) {
			return true;
		} else {
			return false;
		}
	}
	
	public void regulatesThe(Exchange offer) {
		offer.setPricePerUnit(this.setsExchangeRate(offer));
	}


	public double setsExchangeRate(Exchange offer) {
		// first execute super method, if assets is not dealt with, it will
		// return zero.
		double exchangeRate = super.setsExchangeRate(offer);
		if (exchangeRate == 0) {
			switch (offer.getWhatIsGiven()) {
			case OBLIGATION: // this is the obligation to protect
				switch (offer.getWhatIsTaken()) {
				case OBLIGATION: // this is the obligation to work for the
									// protector
					exchangeRate = exchangeRates[AssetList.OBLIGATION.ordinal()];
					break;
				default:
					exchangeRate = 0;
					break;
				}
				break;
			case LABOR:// this is when the serf household sells the labor to the
				// nobleman
				switch (offer.getWhatIsTaken()) {
				case OBLIGATION: // the nobleman gives back part of the
									// obligations
									// due
					exchangeRate = exchangeRates[AssetList.LABOR.ordinal()];
					break;
				default:
					exchangeRate = 0;
					break;
				}
				break;
			case RESIDENCE_LAND:
				switch (offer.getWhatIsTaken()) {
				case SILVER:
					exchangeRate = -exchangeRates[AssetList.RESIDENCE_LAND.ordinal()];
					// because the offer is negative in this case, the price
					// should
					// given here as negative, to have a positive total cost
					break;
				default:
					exchangeRate = 0;
					break;
				}
				break;
			default:
				exchangeRate = 0;
				break;
			}
		}
		return exchangeRate;
	}
}
