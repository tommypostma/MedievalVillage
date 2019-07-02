package deusExMachina;

import java.util.Random;

public class ExP {
	public static final int workingWeeksInAYear = 52;
	
	public static final double townMerchantsSilverReserve = 1500;

	public final static int adultsAtManor = 200;
	// the number of members of the nobleman's house
	public final static int numberOfDependentHouseholds = 133;
	// how many serf households are on the domain
	public final static double typicalParcelOfSerfResidence = 9;
	// the average area for a serf household (residence land)
	public final static double typicalDemesneFraction = 0.37;
	// a value that helps us to compute the arable land directly managed by
	// nobleman
	public final static double manorialDomainSurface = numberOfDependentHouseholds * typicalParcelOfSerfResidence
			+ numberOfDependentHouseholds * typicalParcelOfSerfResidence * typicalDemesneFraction;
	// the demesne of the nobleman is computed proportional with the number of
	// serfs and their land - for the simulation purposes (it increases when the
	// number of serfs increases)
	public static final double initialSilverValueForManorHouse = 5000;
	public static final double manorHouseSilverReserve = 50;
	public static final double initialTownProductStockLevelForManorHouse = 1500;
	
	public static final double manorHouseSafetyLevel = 3.0/4;
	public static final double manorHouseRestockLevel = 1.0/2;
	public static final double serfHouseholdSafetyLevel = 7.0/8;
	public static final double serfHouseholdRestockLevel = 2.0/3;
	public static final double serfHouseholdSilverReserve = 5.0;
	public final static int typicalCorveeHoursPerWeekPerAdult = 20;
	// this is how many hours each household adult has to give to the Manor
	// House
	public final static double typicalSilverQuantityPerSerfHousehold = 50;
	// when a serf household is created, this is the level of silver it has
	// initially
	public final static double typicalGoodsQuantityPerSerf = 10.0;
	public final static double typicalGoodsQuantityForYeoman = 50.0;
	// the same for the level of manufactured goods in the household
	// we set how much food (agricultural produce) a person eats each week
	public final static double weeklyRationManorPeople = 7;
	public final static double weeklyRationForSerfs = 3;
	// of course, the manor people eat more!
	public final static double goodsDepletionRate = 0.22; //was 2.2
	// this is a parameter that you can play with. If the depletion is higher,
	// the serfs will get richer slower, because they have to use their silver
	// to buy goods more often and in bigger quantity

	public final static int startYear = 1000;
	public final static int endOfTheWorldYear = startYear + 25; //25
	// we define the period of history when the simulation takes place

	private static Random rand = new Random();

	// this method is used to give the random number of adults per serf
	// household when this household
	// is created
	public static int makeSerfs() {
		final double typicalNumberOfSerfsPerHousehold = 8; // an average, or
															// "mean"
		final double serfPerHouseStDev = 6; // the standard deviation
		final int minNumberOfSerfs = 2; // they should at least two of them when
										// the household is created
		// we use a normal distribution (Gaussian) to generate the number of
		// serfs
		// for this, the Random class has a method named nextGaussian()
		int madeSerfs = (int) (rand.nextGaussian() * serfPerHouseStDev + typicalNumberOfSerfsPerHousehold);
		// however, the above formula can generate sometimes negative values
		// therefore, we make sure that there are at least a minimum number of
		// serfs returned
		if (madeSerfs < minNumberOfSerfs) {
			return minNumberOfSerfs;
		} else {
			return madeSerfs;
		}
	}

	// this method creates the random surface of a parcel given to a serf by the
	// nobleman
	// it is a using a normal distribution, and cannot be smaller than 5
	// the mean is the constant typicalParcelOfSerfResidence
	public static double createParcelForSerf() {
		double parcelStDev = 3;
		double minParcel = 5;
		double parcel = rand.nextGaussian() * parcelStDev + typicalParcelOfSerfResidence;
		if (parcel < minParcel) {
			return minParcel;
		} else {
			return parcel;
		}
	}

	// because the labor delivered to the nobleman can be dependent on the
	// weather, sickness, etc
	// a method that introduces some randomness in how much labor is given to
	// the nobleman is necessary
	public static double corveeVariability(double total) {
		double stochasticPart = 0.03 * total * rand.nextGaussian();
		if (stochasticPart < (-1.0 / 3) * total) {
			return (-1.0 / 3) * total;
		}
		if (stochasticPart > (1.0 / 3) * total) {
			return (1.0 / 3) * total;
		}
		return stochasticPart;
	}

	// This method is calculating the production, given the land surface and
	// hours worked
	public static double productionIf(double theSurfaceIs, double hoursWorked) {
		return theSurfaceIs * hoursWorked / 100;
	}

	// we consider that the manufactured good depletion is a bit random
	// (Gaussian distribution)
	public static double goodsDepletionRatePerSerfPerWeek() {
		double depletion = goodsDepletionRate;
		double stochasticPart = 0.09 * depletion * rand.nextGaussian();
		// you can change also the variability level of the depletion
		if (stochasticPart < (-1.0 / 3) * depletion) {
			return (-1.0 / 3) * depletion + depletion;
		}
		if (stochasticPart > (1.0 / 3) * depletion) {
			return (1.0 / 3) * depletion + depletion;
		}
		return stochasticPart + depletion;

	}
}
