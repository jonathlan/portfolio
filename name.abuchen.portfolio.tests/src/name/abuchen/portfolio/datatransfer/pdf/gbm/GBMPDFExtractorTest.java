package name.abuchen.portfolio.datatransfer.pdf.gbm;

import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.dividend;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasAmount;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasCurrencyCode;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasDate;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasExDate;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasFees;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasGrossValue;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasIsin;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasName;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasNote;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasShares;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasSource;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasTaxes;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasTicker;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasWkn;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.purchase;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.security;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countAccountTransactions;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countAccountTransfers;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countBuySell;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countItemsWithFailureMessage;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countSecurities;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countSkippedItems;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.pdf.GBMPDFExtractor;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.model.Client;

@SuppressWarnings("nls")
public class GBMPDFExtractorTest
{
    @Test
    public void testEstadoDeCuenta01()
    {
        var extractor = new GBMPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        var results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "EstadoDeCuenta01.txt"), errors);

        assertThat(errors, empty());
        assertThat(countSecurities(results), is(3L));
        assertThat(countBuySell(results), is(3L));
        assertThat(countAccountTransactions(results), is(3L));
        assertThat(countAccountTransfers(results), is(0L));
        assertThat(countItemsWithFailureMessage(results), is(0L));
        assertThat(countSkippedItems(results), is(0L));
        assertThat(results.size(), is(9));
        new AssertImportActions().check(results, "MXN");

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("GBMF2BF"), //
                        hasName("GBMF2 BF"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("FUNO11"), //
                        hasName("FUNO 11"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("TERRA13"), //
                        hasName("TERRA 13"), //
                        hasCurrencyCode("MXN"))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-05-04T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta01.txt"), //
                        hasNote("Folio: 52898941"), //
                        hasAmount("MXN", 74.32), hasGrossValue("MXN", 74.32), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-05-10T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta01.txt"), //
                        hasNote("Folio: 1671315"), //
                        hasAmount("MXN", 33.33), hasGrossValue("MXN", 47.61), //
                        hasTaxes("MXN", 14.28), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-05-10T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta01.txt"), //
                        hasNote("Folio: 1689408"), //
                        hasAmount("MXN", 24.63), hasGrossValue("MXN", 24.63), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-05-10T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta01.txt"), //
                        hasNote("Folio: 53698248"), //
                        hasAmount("MXN", 74.33), hasGrossValue("MXN", 74.33), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-05-13T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta01.txt"), //
                        hasNote("Folio: 1737618"), //
                        hasAmount("MXN", 68.75), hasGrossValue("MXN", 98.22), //
                        hasTaxes("MXN", 29.47), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-05-13T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta01.txt"), //
                        hasNote("Folio: 54290716"), //
                        hasAmount("MXN", 74.34), hasGrossValue("MXN", 74.34), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));
    }

    @Test
    public void testEstadoDeCuenta02()
    {
        var extractor = new GBMPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        var results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "EstadoDeCuenta02.txt"), errors);

        assertThat(errors, empty());
        assertThat(countSecurities(results), is(3L));
        assertThat(countBuySell(results), is(1L));
        assertThat(countAccountTransactions(results), is(2L));
        assertThat(countAccountTransfers(results), is(0L));
        assertThat(countItemsWithFailureMessage(results), is(0L));
        assertThat(countSkippedItems(results), is(0L));
        assertThat(results.size(), is(6));
        new AssertImportActions().check(results, "MXN");

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("GBMF2BF"), //
                        hasName("GBMF2 BF"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("VWO"), //
                        hasName("VWO"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("VEA"), //
                        hasName("VEA"), //
                        hasCurrencyCode("MXN"))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-06-24T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta02.txt"), //
                        hasNote("Folio: 30895065"), //
                        hasAmount("MXN", 17.79), hasGrossValue("MXN", 19.77), //
                        hasTaxes("MXN", 1.98), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-06-24T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta02.txt"), //
                        hasNote("Folio: 30944223"), //
                        hasAmount("MXN", 78.79), hasGrossValue("MXN", 87.54), //
                        hasTaxes("MXN", 8.75), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-06-24T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta02.txt"), //
                        hasNote("Folio: 61475171"), //
                        hasAmount("MXN", 74.45), hasGrossValue("MXN", 74.45), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));
    }

    @Test
    public void testEstadoDeCuenta03()
    {
        var extractor = new GBMPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        var results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "EstadoDeCuenta03.txt"), errors);

        assertThat(errors, empty());
        assertThat(countSecurities(results), is(3L));
        assertThat(countBuySell(results), is(2L));
        assertThat(countAccountTransactions(results), is(2L));
        assertThat(countAccountTransfers(results), is(0L));
        assertThat(countItemsWithFailureMessage(results), is(0L));
        assertThat(countSkippedItems(results), is(0L));
        assertThat(results.size(), is(7));
        new AssertImportActions().check(results, "MXN");

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("GBMF2BF"), //
                        hasName("GBMF2 BF"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("VOO"), //
                        hasName("VOO"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("FIBRAPL14"), //
                        hasName("FIBRAPL 14"), //
                        hasCurrencyCode("MXN"))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-07-02T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta03.txt"), //
                        hasNote("Folio: 31968894"), //
                        hasAmount("MXN", 67.04), hasGrossValue("MXN", 74.49), //
                        hasTaxes("MXN", 7.45), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-07-02T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta03.txt"), //
                        hasNote("Folio: 63309965"), //
                        hasAmount("MXN", 74.46), hasGrossValue("MXN", 74.46), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-07-30T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta03.txt"), //
                        hasNote("Folio: 1951276"), //
                        hasAmount("MXN", 81.47), hasGrossValue("MXN", 116.39), //
                        hasTaxes("MXN", 34.92), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-07-30T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta03.txt"), //
                        hasNote("Folio: 69843400"), //
                        hasAmount("MXN", 74.54), hasGrossValue("MXN", 74.54), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));
    }

    @Test
    public void testEstadoDeCuenta04()
    {
        var extractor = new GBMPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        var results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "EstadoDeCuenta04.txt"), errors);

        assertThat(errors, empty());
        assertThat(countSecurities(results), is(3L));
        assertThat(countBuySell(results), is(2L));
        assertThat(countAccountTransactions(results), is(3L));
        assertThat(countAccountTransfers(results), is(0L));
        assertThat(countItemsWithFailureMessage(results), is(0L));
        assertThat(countSkippedItems(results), is(0L));
        assertThat(results.size(), is(8));
        new AssertImportActions().check(results, "MXN");

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("GBMF2BF"), //
                        hasName("GBMF2 BF"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("TERRA13"), //
                        hasName("TERRA 13"), //
                        hasCurrencyCode("MXN"))));

        // check security
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("FUNO11"), //
                        hasName("FUNO 11"), //
                        hasCurrencyCode("MXN"))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-08-05T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta04.txt"), //
                        hasNote("Folio: 2019312"), //
                        hasAmount("MXN", 52.45), hasGrossValue("MXN", 74.93), //
                        hasTaxes("MXN", 22.48), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-08-05T00:00"), hasShares(2.00), //
                        hasSource("EstadoDeCuenta04.txt"), //
                        hasNote("Folio: 71302078"), //
                        hasAmount("MXN", 74.56), hasGrossValue("MXN", 74.56), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-08-09T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta04.txt"), //
                        hasNote("Folio: 2082726"), //
                        hasAmount("MXN", 33.66), hasGrossValue("MXN", 48.08), //
                        hasTaxes("MXN", 14.42), hasFees("MXN", 0.00))));

        // check dividend transaction
        assertThat(results, hasItem(dividend( //
                        hasDate("2021-08-09T00:00"), hasExDate(null), //
                        hasShares(0.00), //
                        hasSource("EstadoDeCuenta04.txt"), //
                        hasNote("Folio: 2105960"), //
                        hasAmount("MXN", 24.77), hasGrossValue("MXN", 24.77), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));

        // check buy sell transaction
        assertThat(results, hasItem(purchase( //
                        hasDate("2021-08-09T00:00"), hasShares(1.00), //
                        hasSource("EstadoDeCuenta04.txt"), //
                        hasNote("Folio: 71993813"), //
                        hasAmount("MXN", 37.28), hasGrossValue("MXN", 37.28), //
                        hasTaxes("MXN", 0.00), hasFees("MXN", 0.00))));
    }
}
