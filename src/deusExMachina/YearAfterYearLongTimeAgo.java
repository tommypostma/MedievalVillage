package deusExMachina;

/**
 * @author ${Nick Szirbik}
 *
 * ${practical 6 solution with new output file for linear regression analysis}
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class YearAfterYearLongTimeAgo {
	/*
	 * the object variables that will reference the economic actors are declared
	 * as static variables. First, the household of the village's nobleman is
	 * declared
	 */
	static ManorHouse leSeigneur;
	/*
	 * observation: the object leSeigneur is not instantiated yet! next we
	 * declare the array that will keep the references to the serf households
	 */
	static DependentHousehold[] serfs;
	/*
	 * observation: the array cells are not allocated yet! finally, we declare
	 * the object variable that will reference the economic regulator
	 */
	static FreeHousehold[] yeomen;
	/*
	 * this is the variable that contains the array of free households on the
	 * domain - they do not pay corvée, but they will have to offer men at arms
	 */
	static LocalChurch theLocalChurch;
	/*
	 * for practical 5 and its homework, this is the new object to be created
	 */
	static ExternalTradingPartner townMerchants;
	// town church which regulates the monthly town market
	static ExchangeRegulator theTownChurch;

	public static void main(String[] args) throws IOException {
		// the economic actors of this application are _created_ first
		theLocalChurch = new LocalChurch(3.0, 2.0, 3.5, -1.0, 25000.0, 0.25, 80.0);
		leSeigneur = new ManorHouse(ExP.manorialDomainSurface, ExP.adultsAtManor);
		newSerfsAreCreated();
		yeomen = new FreeHousehold[0]; // initially, there are no households
		townMerchants = new ExternalTradingPartner();
		// new town church with higher lower exchange rate for town products and
		// higher for agri products
		theTownChurch = new ExchangeRegulator(1.5, 4.0);

		int week, year = ExP.startYear;
		while (year <= ExP.endOfTheWorldYear) {
			System.out.print(year + " starts");
			establishHowMuchEachSerfHouseCanWorkThisYear();
			establishHowMuchEachYeomanCanWorkThisYear();
			week = 1;
			do {
				// each weak, the serfs sell part of their labor to the nobleman
				sellWeeklyCorveeLaborToSeigneurAndReduceObligations();
				// all economic actors produce agricultural produce
				produceForAWeek(week);
				/*
				 * during the week, some of the agricultural produce is consumed
				 * by all households
				 */
				consumeForAWeek();
				/*
				 * each week, there is a local market in the village the serfs
				 * sell their food surplus to the nobleman, in exchange for
				 * silver and the serfs buy manufactured products from the
				 * nobleman, in exchange for silver
				 */
				tradeAtTheLocalMarket();
				/*
				 * the nobleman goes to town to trade himself (agri-produce
				 * against silver and silver against manufactured produce, but
				 * this is not coded yet here...
				 */
				if (week % 4 == 0) {
					// System.out.println(" --- Town Market in week " + week);
					tradeAtTheTownMarket();
				}
				week++;
			} while (week <= ExP.workingWeeksInAYear);
			theRichSerfsTryToBuyTheirResidenceLand();
			System.out.println("-----and ends");
			year++;
		}
		// finally, we want to know the economical situation, by knowing how
		// much silver each household has
		reportEconomicalSituationOfTheVillage();
		// and we save the final situation in a CSV file
		exportSerfDataTo("serfsEconomicSituation.csv");
		exportYeomenDataTo("yeomensEconomicSituation.csv");
		exportVillageStocksTo("villageAssets.csv");
	}

	private static void newSerfsAreCreated() {
		serfs = new DependentHousehold[ExP.numberOfDependentHouseholds];
		for (int i = 0; i < serfs.length; i++) {
			serfs[i] = new DependentHousehold(ExP.makeSerfs());
			leSeigneur.allocatesLandForResidence(serfs[i]);
		}
		/*
		 * each serf household creation will diminish the directly owned land of
		 * the manor house, which will remain with a part called demesne
		 */
		for (DependentHousehold serf : serfs) {
			serf.reportAdultsAndBoundLandSurface();
		} // we print first the initial situation of the village, in terms of
			// land and adult serf numbers, and also print the demesne area of
			// the nobleman
		System.out.println("Manor House has a demesne of " + leSeigneur.getDemesne());
	}

	// at the beginning of each year, it is calculated how much work the
	// serf households have to give to the nobleman
	private static void establishHowMuchEachSerfHouseCanWorkThisYear() {
		Exchange obligationTransaction;
		for (DependentHousehold house : serfs) {
			// first, the household is told by the church how much they have to
			// work
			// for the nobleman
			house.computeYearlyLaborPotentialBy(theLocalChurch);
			// the nobleman makes an offer, where the exchange item is an
			// obligation
			// this will be like a contract, where the nobleman is obliged to
			// protect the serfs
			// and the serfs are obliged to work on the nobleman's demesne land
			obligationTransaction = leSeigneur.offers(AssetList.OBLIGATION, AssetList.OBLIGATION);
			// to avoid that the serfs are over-obliged, the regulator makes
			// sure that the
			// exploitation is bearable
			theLocalChurch.regulatesThe(obligationTransaction);
			// The serf household has to accept this obligation now
			house.acceptsPartOrWholeOf(obligationTransaction);
			// and the nobleman is settling the transaction
			leSeigneur.settlesTransactionOver(obligationTransaction);
		}
	}

	private static void establishHowMuchEachYeomanCanWorkThisYear() {
		for (FreeHousehold yeoman : yeomen) {
			yeoman.computeYearlyLaborPotentialBy(theLocalChurch);
		}
	}

	private static void sellWeeklyCorveeLaborToSeigneurAndReduceObligations() {
		// this is just a transaction, in the same pattern as the rest
		// first, a party offers something, creating an exchange object
		// second, the regulator regulates by setting the exchange rate (price)
		// in the exchange object
		// next, the other party accepts (or not) the offer
		// finally, the first party settles the transaction
		Exchange corvee;
		for (DependentHousehold serfHousehold : serfs) {
			corvee = serfHousehold.offers(AssetList.LABOR, /* in exchange for */AssetList.OBLIGATION);
			theLocalChurch.regulatesThe(corvee);
			leSeigneur.acceptsPartOrWholeOf(corvee);
			serfHousehold.settlesTransactionOver(corvee);
		}
	}

	// by working their own land and the land of the nobleman, agricultural
	// produce is generated
	// for both types of households, Manor House, and Dependent households
	private static void produceForAWeek(int weekNo) {
		leSeigneur.produces();
		for (DependentHousehold serfHousehold : serfs) {
			serfHousehold.produces(weekNo);
		}
		for (FreeHousehold yeoman : yeomen) {
			yeoman.produces(weekNo);
		}
	}

	// in the consumption phase, we have to be careful not to get negative
	// stocks
	private static void consumeForAWeek() {
		leSeigneur.consumes();
		leSeigneur.depleteGoodsWithTheSameAmountAs();
		for (DependentHousehold serfHousehold : serfs) {
			serfHousehold.consumes();
			serfHousehold.depleteGoodsWithTheSameAmountAs();
		}
		for (FreeHousehold yeoman : yeomen) {
			yeoman.consumes();
			yeoman.depleteGoodsWithTheSameAmountAs();
		}
	}

	private static void tradeAtTheLocalMarket() {
		// the nobleman buys first agricultural produce from his serfs, and pays
		// with silver
		// the pattern of exchange is exactly the same as for any transaction
		// first offer, then regulate, then accept, and finally settle
		for (DependentHousehold serfHousehold : serfs) {
			Exchange surplus = serfHousehold.offers(AssetList.AGRI_PRODUCE, /* for */ AssetList.SILVER);
			theLocalChurch.regulatesThe(surplus);
			leSeigneur.acceptsPartOrWholeOf(surplus);
			serfHousehold.settlesTransactionOver(surplus);
		}
		// after that Seigneur, sells manufactured goods from his reserves, and
		// the serfs pay with silver
		for (DependentHousehold serfHousehold : serfs) {
			Exchange goods = leSeigneur.offers(AssetList.TOWN_PRODUCT, /* for */ AssetList.SILVER);
			theLocalChurch.regulatesThe(goods);
			serfHousehold.acceptsPartOrWholeOf(goods);
			leSeigneur.settlesTransactionOver(goods);
		}
	}

	private static void tradeAtTheTownMarket() {
		// first exchange - Seigneur sells produce for silver
		Exchange agriProduce = leSeigneur.offers(AssetList.AGRI_PRODUCE, AssetList.SILVER);
		theTownChurch.regulatesThe(agriProduce);
		townMerchants.acceptsPartOrWholeOf(agriProduce);
		leSeigneur.settlesTransactionOver(agriProduce);

		// second exchange - Seigneur pays silver for town goods
		Exchange townGoods = townMerchants.offers(AssetList.TOWN_PRODUCT, AssetList.SILVER);
		theTownChurch.regulatesThe(townGoods);
		leSeigneur.acceptsPartOrWholeOf(townGoods);
		townMerchants.settlesTransactionOver(townGoods);

		// Yeomen go to the town market
		for (FreeHousehold yeoman : yeomen) {
			// First, they sell produce for silver
			Exchange agriSurplus = yeoman.offers(AssetList.AGRI_PRODUCE, AssetList.SILVER);
			theTownChurch.regulatesThe(agriSurplus);
			townMerchants.acceptsPartOrWholeOf(agriSurplus);
			yeoman.settlesTransactionOver(agriSurplus);

			// Then, they buy town products
			Exchange townProducts = townMerchants.offers(AssetList.TOWN_PRODUCT, AssetList.SILVER);
			theTownChurch.regulatesThe(townProducts);
			yeoman.acceptsPartOrWholeOf(townProducts);
			townMerchants.settlesTransactionOver(townProducts);
		}

	}

	// this method is invoked at the end of each year
	private static void theRichSerfsTryToBuyTheirResidenceLand() {
		/*
		 * we look for those serfs that according to the church are rich enough
		 * to buy their residence land
		 */
		for (DependentHousehold serf : serfs) {
			if (serf.canBuyItsFreedomAccordingTo(theLocalChurch)) {
				Exchange landOffer = leSeigneur.offers(AssetList.RESIDENCE_LAND, AssetList.SILVER);
				theLocalChurch.regulatesThe(landOffer);
				serf.acceptsPartOrWholeOf(landOffer);
				leSeigneur.settlesTransactionOver(landOffer);
			}
		}
		makesFreeYeomenOutOfSerfs();
	}

	private static void makesFreeYeomenOutOfSerfs() {
		DependentHousehold[] updatedSerfsArray = null;
		FreeHousehold[] updatedYeomenArray = null;
		// first count how many serfs are freed
		int freedSerfs = 0;
		for (DependentHousehold serf : serfs) {
			if (serf.hasOwnLand()) {
				freedSerfs++;
			}
		}
		if (freedSerfs > 0) {
			updatedSerfsArray = new DependentHousehold[serfs.length - freedSerfs];
			updatedYeomenArray = new FreeHousehold[yeomen.length + freedSerfs];

			fillUpdatedArrays(updatedSerfsArray, updatedYeomenArray);
			System.out.print(" there are " + freedSerfs + " freedSerfs this year " + yeomen.length);
			serfs = updatedSerfsArray;
			yeomen = updatedYeomenArray;
		}
	}

	// this method makes two new arrays for serfs and yeomen
	private static void fillUpdatedArrays(DependentHousehold[] ser, FreeHousehold[] yeo) {
		// move existing yeomen to the new array
		for (int yindex = 0; yindex < yeomen.length; yindex++) {
			yeo[yindex] = yeomen[yindex];
		}
		int yeoCounter = yeomen.length;// this is the first position in the
		int serCounter = 0; // updated array yeo
		for (DependentHousehold serf : serfs) {
			if (serf.hasOwnLand()) {
				yeo[yeoCounter] = serf.becomesFree();
				yeoCounter++;
			} else {
				ser[serCounter] = serf;
				serCounter++;
			}
		}
	}

	// we go through the households and make them report their silver
	private static void reportEconomicalSituationOfTheVillage() {
		for (DependentHousehold serfHousehold : serfs) {
			serfHousehold.reportsSilver();
		}
		leSeigneur.reportsSilver();
		System.out.println("there are " + serfs.length + " SERF households left");
		System.out.println("and " + yeomen.length + " households became FREE...");
	}

	// to be able to visualize and analyze the data in R
	private static void exportSerfDataTo(String filename) throws IOException {
		Path path = Paths.get(filename);
		BufferedWriter writer = Files.newBufferedWriter(path);

		writer.write("Serf, FamilySize, PlotSize, SilverStock\n");
		for (DependentHousehold serf : serfs) {
			writer.write(serf.houseNo + ", " + serf.getFamilySize() + ", " + serf.getStockOf(AssetList.RESIDENCE_LAND)
					+ ", " + serf.getStockOf(AssetList.SILVER) + "\n");
		}

		writer.close();
	}

	private static void exportYeomenDataTo(String filename) throws IOException {
		Path path = Paths.get(filename);
		BufferedWriter writer = Files.newBufferedWriter(path);

		writer.write("Yeomen, FamilySize, OwnedLand, TimeToEarnFreedom, GoodsSoldAsSerf\n");
		for (FreeHousehold yeoman : yeomen) {
			writer.write(yeoman.getHouseNo() + "," + yeoman.howManyAdults() + "," + yeoman.getOwnedLand() + ","
					+ yeoman.getSerfdomPeriod() + "," + yeoman.getTotalPreviousFoodContributionAsSerfHousehold()
					+ "\n");
		}
		writer.close();
	}

	private static void exportVillageStocksTo(String filename) throws IOException {
		Path path = Paths.get(filename);
		BufferedWriter writer = Files.newBufferedWriter(path);
		// Write first line of .csv file
		writer.write("Number, FamilySize, ");
		for (AssetList asset : AssetList.values()) {
			if (asset.ordinal() < (AssetList.values().length - 1)) {
				writer.write(asset + ", ");
			} else {
				writer.write(asset + "\n");
			}
		}
		// Write line for leSeigneur
		writer.write("0, " + leSeigneur.howManyAdults() + ", " + leSeigneur.assetsToCsv() + "\n");
		// Write for all serfs
		for (DependentHousehold serf : serfs) {
			writer.write(serf.houseNo + ", " + serf.howManyAdults() + ", " + serf.assetsToCsv() + "\n");
		}
		// Write for free households
		for (FreeHousehold yeoman : yeomen) {
			writer.write(yeoman.getHouseNo() + ", " + yeoman.howManyAdults() + ", " + yeoman.assetsToCsv() + "\n");
		}
		// Finish file with an empty line
		writer.write("\n");
		writer.close();
	}

}
