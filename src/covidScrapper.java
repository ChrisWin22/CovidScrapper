import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class covidScrapper {

    public static class State{
        String name;
        double popInMillions;
        int newDailyCases;
        int totalDeaths;
        int totalCases;
        int newDeaths;
        int utahEq;

        public State(){}

        public State(String name, double pop, int newDC, int td, int tc, int nd){
            this.name = name;
            popInMillions = pop;
            newDailyCases = newDC;
            totalDeaths = td;
            totalCases = tc;
            newDeaths = nd;
        }
    }

    public static void main(String[] args) throws IOException {
        ArrayList<State> unitedStates = new ArrayList<>();

        URL url = new URL("https://www.theguardian.com/world/ng-interactive/2020/sep/21/coronavirus-covid-19-map-us-cases-deaths-state-by-state");
        String html = new String(url.openStream().readAllBytes());

        String outerDiv = "<div class=\"co-tbody\">";
        String stateClass = "class=\"co-state--container\">";
        String stateNameClass = "<span class=\"co-state--name\">";
        String stateNameClassEnd = "</span>";
        String statePopClass = "<span class=\"co-state--population\">";
        String statePopClassEnd = "residents</span>";
        String casesHeader = "<b>Cases:</b>";
        String casesEnd = "</span>";
        String newCases = "<span>";
        String newCasesEnd = "</span>";
        String deathsHeader = "<b>Deaths:</b>";
        String deathsEnd = "</span>";
        String newDeaths = "<span>";
        String newDeathsEnd = "</span>";

        int startingIndex = html.indexOf(outerDiv, 0) + outerDiv.length();

        State utah = new State();

        while(true){
            int stateIndex = html.indexOf(stateClass, startingIndex) + stateClass.length();

            //Get state name
            int stateNameIndex = html.indexOf(stateNameClass, stateIndex) + stateNameClass.length();
            int stateNameEndIndex = html.indexOf(stateNameClassEnd, stateNameIndex);
            String stateName = html.substring(stateNameIndex, stateNameEndIndex);

            if(stateName.compareTo("American Samoa") != 0 && stateName.compareTo("Guam") != 0 && stateName.compareTo("Puerto Rico") != 0) {

                //Get state population
                int statePopIndex = html.indexOf(statePopClass, stateNameEndIndex) + statePopClass.length();
                int statePopEndIndex = html.indexOf(statePopClassEnd, statePopIndex) - 2;
                double pop = Double.parseDouble(html.substring(statePopIndex, statePopEndIndex));

                //Get total cases
                int totalCasesIndex = html.indexOf(casesHeader, statePopEndIndex) + casesHeader.length();
                int totalCasesEndIndex = html.indexOf(casesEnd, totalCasesIndex);
                int totalCases = Integer.parseInt(stripCommas(html.substring(totalCasesIndex, totalCasesEndIndex)));

                //Get new daily cases
                int newCasesIndex = html.indexOf(newCases, totalCasesEndIndex) + newCases.length() + 1;
                int newCasesEndIndex = html.indexOf(newCasesEnd, newCasesIndex);
                int newStateCases = Integer.parseInt(stripCommas(html.substring(newCasesIndex, newCasesEndIndex)));

                //Get total deaths
                int totalDeathIndex = html.indexOf(deathsHeader, newCasesEndIndex) + deathsHeader.length();
                int totalDeathEndIndex = html.indexOf(deathsEnd, totalDeathIndex);
                int totalDeaths = Integer.parseInt(stripCommas(html.substring(totalDeathIndex, totalDeathEndIndex)));

                //Get new daily cases
                int newDeathsIndex = html.indexOf(newDeaths, totalDeathEndIndex) + newDeaths.length() + 1;
                int newDeathsEndIndex = html.indexOf(newDeathsEnd, newDeathsIndex);
                int newStateDeaths = Integer.parseInt(stripCommas(html.substring(newDeathsIndex, newDeathsEndIndex)));


                if (stateName.compareTo("Utah") == 0) {
                    utah = new State(stateName, pop, newStateCases, totalDeaths, totalCases, newStateDeaths);
                } else {
                    unitedStates.add(new State(stateName, pop, newStateCases, totalDeaths, totalCases, newStateDeaths));
                }

                if (stateName.compareTo("New Hampshire") == 0)
                    break;
            }
            startingIndex = stateNameEndIndex;

        }

        for(State state : unitedStates){
            state.utahEq = (int) ((utah.popInMillions * state.newDailyCases) / state.popInMillions);
        }

        unitedStates.sort(new Comparator<State>() {
            @Override
            public int compare(State state, State t1) {
                return (state.utahEq) - (t1.utahEq);
            }
        });

        prettyPrint(utah, unitedStates);

    }

    public static String stripCommas(String s){
        return s.replace(",", "").replace(" ", "");
    }

    public static void prettyPrint(State utah, ArrayList<State> usa){
        System.out.println();
        System.out.printf("%-21S| %-12S| %-8S| %-13S| %-9S| %-5S|\n", "Name", "Cases Today", "Utah EQ", "Deaths Today", "Death EQ", "Pop");
        System.out.println("-".repeat(79));
        System.out.printf("%-21s| %-12d| %-8s| %-13d| %-9d| %-5.1f|\n", utah.name, utah.newDailyCases, utah.newDailyCases, utah.newDeaths, utah.newDeaths, utah.popInMillions);

        for(State state : usa){
            int deathEQ = (int) ((utah.popInMillions * state.newDeaths) / state.popInMillions);
            System.out.printf("%-21s| %-12d| %-8d| %-13d| %-9d| %-5.1f|\n", state.name, state.newDailyCases, state.utahEq, state.newDeaths, deathEQ, state.popInMillions);
        }
    }
}
