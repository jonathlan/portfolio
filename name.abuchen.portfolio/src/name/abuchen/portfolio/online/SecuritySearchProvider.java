package name.abuchen.portfolio.online;

import java.io.IOException;
import java.util.List;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.Security;

public interface SecuritySearchProvider
{
    public interface ResultItem
    {
        String getName();

        String getSymbol();

        String getIsin();

        String getWkn();

        String getType();

        String getExchange();

        default String getOnlineId()
        {
            return null;
        }

        void applyTo(Security security);
    }

    public enum Type
    {
        ALL(Messages.LabelSearchAll), SHARE(Messages.LabelSearchShare), BOND(Messages.LabelSearchBond);

        private final String label;

        private Type(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    String getName();

    List<ResultItem> search(String query, Type type) throws IOException;
}
