package name.abuchen.portfolio.datatransfer.pdf;

import static name.abuchen.portfolio.datatransfer.ExtractorUtils.checkAndSetTax;
import static name.abuchen.portfolio.util.TextUtil.trim;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import name.abuchen.portfolio.datatransfer.DocumentContext;
import name.abuchen.portfolio.datatransfer.ExtractorUtils;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Block;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.DocumentType;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Transaction;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.util.AdditionalLocales;

/**
 * @formatter:off
 * @implNote GBM (Grupo Bursátil Mexicano, GBM+) is a Mexican broker.
 *           The currency is MXN (Mexican Peso).
 *
 * @implSpec The monthly account statement (Estado de Cuenta) contains the transactions
 *           in the "DESGLOSE DE MOVIMIENTOS" section.
 *           All security currencies are in MXN.
 *           No ISIN or WKN is provided. The security name is the ticker symbol (EMISORA)
 *           followed by the series. For securities of the international quotation system (SIC)
 *           the series is "*" and is not part of the security name.
 *           The ticker symbol is the EMISORA concatenated with the series (e.g. "GBMF2BF"),
 *           following the BMV listing convention, so that existing securities with an
 *           exchange suffix (e.g. "GBMF2BF.MX") are matched during import.
 *
 *           The transaction rows only contain the day of the operation and the day of the
 *           settlement (FECHA OPER/LIQ). The settlement day is used as the transaction date.
 *           Month and year are taken from the statement period
 *           (e.g. "DEL 1 AL 31 DE MAYO DE 2021").
 *
 *           Tax withholding rows (RETENCION ISR POR RESULTADO FISCAL, ISR 10 % POR DIVIDENDOS SIC)
 *           are collected upfront and merged into the matching dividend transaction.
 *
 * @formatter:on
 */
@SuppressWarnings("nls")
public class GBMPDFExtractor extends AbstractPDFExtractor
{
    private static final String MXN = "MXN";

    public GBMPDFExtractor(Client client)
    {
        super(client);

        addBankIdentifier("GRUPO BURSATIL MEXICANO S.A. DE C.V., CASA DE BOLSA");
        addBankIdentifier("gbm.com.mx");

        addBuySellTransaction();
        addDividendTransaction();
    }

    @Override
    public String getLabel()
    {
        return "GBM (Grupo Bursátil Mexicano)";
    }

    private void addBuySellTransaction()
    {
        final var type = new DocumentType("DESGLOSE DE MOVIMIENTOS", this::parseDocumentContext);
        this.addDocumentTyp(type);

        var pdfTransaction = new Transaction<BuySellEntry>();

        var firstRelevantLine = new Block("^[\\d]{2}\\/[\\d]{2} [\\d]+ Compra Soc\\. de Inv\\. \\- Cliente .*$");
        type.addBlock(firstRelevantLine);
        firstRelevantLine.set(pdfTransaction);

        pdfTransaction //

                        .subject(() -> new BuySellEntry(PortfolioTransaction.Type.BUY))

                        // @formatter:off
                        // 04/04 52898941 Compra Soc. de Inv. - Cliente GBMF2 BF 2 37.159019 0.00 0.00 0.00 74.32 26.72
                        // @formatter:on
                        .section("date", "note", "name", "serie", "shares", "amount") //
                        .documentContext("month", "year") //
                        .match("^[\\d]{2}\\/(?<date>[\\d]{2}) (?<note>[\\d]+) " //
                                        + "Compra Soc\\. de Inv\\. \\- Cliente " //
                                        + "(?<name>[A-Z0-9]+) (?<serie>[A-Z0-9\\*]+) " //
                                        + "(?<shares>[\\.,\\d]+) [\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ " //
                                        + "(?<amount>[\\.,\\d]+) [\\.,\\d]+$") //
                        .assign((t, v) -> {
                            v.put("currency", asCurrencyCode(MXN));
                            v.put("tickerSymbol", getSecurityTickerSymbol(v.get("name"), v.get("serie")));
                            v.put("name", getSecurityName(v.get("name"), v.get("serie")));
                            t.setSecurity(getOrCreateSecurity(v));

                            t.setDate(asDate(v.get("date") + " " + v.get("month") + " " + v.get("year"), AdditionalLocales.MEXICO));
                            t.setShares(asShares(v.get("shares")));
                            t.setCurrencyCode(asCurrencyCode(MXN));
                            t.setAmount(asAmount(v.get("amount")));
                            t.setNote("Folio: " + v.get("note"));
                        })

                        .wrap(BuySellEntryItem::new);
    }

    private void addDividendTransaction()
    {
        final var type = new DocumentType("DESGLOSE DE MOVIMIENTOS", this::parseDocumentContext);
        this.addDocumentTyp(type);

        var pdfTransaction = new Transaction<AccountTransaction>();

        var firstRelevantLine = new Block("^[\\d]{2}\\/[\\d]{2} [\\d]+ " //
                        + "(Abono efectivo Resultado Fiscal Distribuido" //
                        + "|Abono Reembolso de Capital, Cust\\. Normal" //
                        + "|ABONO DIVIDENDO EMISORA EXTRANJERA) .*$");
        type.addBlock(firstRelevantLine);
        firstRelevantLine.set(pdfTransaction);

        pdfTransaction //

                        .subject(() -> new AccountTransaction(AccountTransaction.Type.DIVIDENDS))

                        // @formatter:off
                        // 06/10 1671315 Abono efectivo Resultado Fiscal Distribuido FUNO 11 0 0.000000 0.00 0.00 0.00 47.61 74.33
                        // 06/10 1689408 Abono Reembolso de Capital, Cust. Normal FUNO 11 0 0.000000 0.00 0.00 0.00 24.63 98.96
                        // 23/24 30895065 ABONO DIVIDENDO EMISORA EXTRANJERA VWO * 0 0.000000 0.00 0.00 0.00 19.77 24.52
                        // @formatter:on
                        .section("date", "note", "type", "name", "serie", "amount") //
                        .documentContext("month", "year") //
                        .match("^[\\d]{2}\\/(?<date>[\\d]{2}) (?<note>[\\d]+) " //
                                        + "(?<type>Abono efectivo Resultado Fiscal Distribuido" //
                                        + "|Abono Reembolso de Capital, Cust\\. Normal" //
                                        + "|ABONO DIVIDENDO EMISORA EXTRANJERA) " //
                                        + "(?<name>[A-Z0-9]+) (?<serie>[A-Z0-9\\*]+) " //
                                        + "[\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ " //
                                        + "(?<amount>[\\.,\\d]+) [\\.,\\d]+$") //
                        .assign((t, v) -> {
                            var context = type.getCurrentContext();
                            var security = v.get("name") + " " + v.get("serie");

                            v.put("currency", asCurrencyCode(MXN));
                            v.put("tickerSymbol", getSecurityTickerSymbol(v.get("name"), v.get("serie")));
                            v.put("name", getSecurityName(v.get("name"), v.get("serie")));
                            t.setSecurity(getOrCreateSecurity(v));

                            t.setDateTime(asDate(v.get("date") + " " + v.get("month") + " " + v.get("year"), AdditionalLocales.MEXICO));
                            t.setCurrencyCode(asCurrencyCode(MXN));
                            t.setAmount(asAmount(v.get("amount")));
                            t.setNote("Folio: " + v.get("note"));

                            // @formatter:off
                            // The tax withholding is booked as a separate row in the statement.
                            // Merge it into the matching dividend transaction.
                            // @formatter:on
                            var taxType = getTaxType(v.get("type"));

                            if (taxType != null)
                            {
                                var taxAmountTransactionHelper = context.getType(TaxAmountTransactionHelper.class).orElseGet(TaxAmountTransactionHelper::new);
                                var item = taxAmountTransactionHelper.findItem(v.getStartLineNumber(), t.getDateTime(), security, taxType);

                                if (item.isPresent())
                                {
                                    var tax = Money.of(item.get().currency, item.get().tax);
                                    t.setMonetaryAmount(t.getMonetaryAmount().subtract(tax));

                                    checkAndSetTax(tax, t, type.getCurrentContext());
                                }
                            }
                        })

                        .wrap(TransactionItem::new);
    }

    private void parseDocumentContext(DocumentContext context, String[] lines)
    {
        // @formatter:off
        // DEL 1 AL 31 DE MAYO DE 2021
        // @formatter:on
        var pPeriod = Pattern.compile("^DEL [\\d]{1,2} AL [\\d]{1,2} DE (?<month>[A-Z]+) DE (?<year>[\\d]{4}).*$");

        // @formatter:off
        // 06/10 26674354 RETENCION ISR POR RESULTADO FISCAL FUNO 11 0 0.000000 0.00 0.00 0.00 14.28 84.67
        // 23/24 30895067 ISR 10 % POR DIVIDENDOS SIC VWO * 0 0.000000 0.00 0.00 0.00 1.98 22.55
        // @formatter:on
        var pTaxAmountTransaction = Pattern.compile("^[\\d]{2}\\/(?<date>[\\d]{2}) [\\d]+ " //
                        + "(?<type>RETENCION ISR POR RESULTADO FISCAL|ISR 10 % POR DIVIDENDOS SIC) " //
                        + "(?<name>[A-Z0-9]+) (?<serie>[A-Z0-9\\*]+) " //
                        + "[\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ [\\.,\\d]+ " //
                        + "(?<tax>[\\.,\\d]+) [\\.,\\d]+$");

        var taxAmountTransactionHelper = new TaxAmountTransactionHelper();
        context.putType(taxAmountTransactionHelper);

        for (var i = 0; i < lines.length; i++)
        {
            var mPeriod = pPeriod.matcher(lines[i]);
            if (mPeriod.matches() && !context.containsKey("month"))
            {
                context.put("month", mPeriod.group("month"));
                context.put("year", mPeriod.group("year"));
            }

            var m = pTaxAmountTransaction.matcher(lines[i]);
            if (m.matches() && context.containsKey("month"))
            {
                var item = new TaxAmountTransactionItem();
                item.line = i + 1;
                item.dateTime = asDate(m.group("date") + " " + context.get("month") + " " + context.get("year"), AdditionalLocales.MEXICO);
                item.type = m.group("type");
                item.security = m.group("name") + " " + m.group("serie");
                item.tax = asAmount(m.group("tax"));
                item.currency = asCurrencyCode(MXN);

                taxAmountTransactionHelper.items.add(item);
            }
        }
    }

    private static String getSecurityName(String name, String serie)
    {
        // @formatter:off
        // For securities of the international quotation system (SIC)
        // the series is "*" and is not part of the security name.
        // @formatter:on
        return "*".equals(serie) ? trim(name) : trim(name) + " " + trim(serie);
    }

    private static String getSecurityTickerSymbol(String name, String serie)
    {
        // @formatter:off
        // The BMV listing symbol is the EMISORA concatenated with the series (e.g. "GBMF2BF").
        // For SIC securities the series is "*" and the EMISORA is already the ticker symbol.
        // @formatter:on
        return "*".equals(serie) ? trim(name) : trim(name) + trim(serie);
    }

    private static String getTaxType(String dividendType)
    {
        if ("Abono efectivo Resultado Fiscal Distribuido".equals(dividendType))
            return "RETENCION ISR POR RESULTADO FISCAL";

        if ("ABONO DIVIDENDO EMISORA EXTRANJERA".equals(dividendType))
            return "ISR 10 % POR DIVIDENDOS SIC";

        return null;
    }

    private static class TaxAmountTransactionHelper
    {
        private List<TaxAmountTransactionItem> items = new ArrayList<>();

        public Optional<TaxAmountTransactionItem> findItem(int line, LocalDateTime dateTime, String security, String type)
        {
            for (TaxAmountTransactionItem item : items)
            {
                if (item.line < line)
                    continue;

                if (item.dateTime.equals(dateTime) && item.security.equals(security) && item.type.equals(type))
                    return Optional.of(item);
            }
            return Optional.empty();
        }
    }

    private static class TaxAmountTransactionItem
    {
        int line;

        LocalDateTime dateTime;
        String type;
        String security;
        String currency;
        long tax;

        @Override
        public String toString()
        {
            return "TaxAmountTransactionItem [line=" + line + ", dateTime=" + dateTime + ", type=" + type + ", security=" + security + ", currency=" + currency + ", tax=" + tax + "]";
        }
    }

    @Override
    protected long asAmount(String value)
    {
        return ExtractorUtils.convertToNumberLong(value, Values.Amount, "es", "MX");
    }

    @Override
    protected long asShares(String value)
    {
        return ExtractorUtils.convertToNumberLong(value, Values.Share, "es", "MX");
    }
}
