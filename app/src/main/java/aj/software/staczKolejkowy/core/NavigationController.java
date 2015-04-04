package aj.software.staczKolejkowy.core;

public interface NavigationController {

    void showStandsActivity(Department department);
    void showInfoActivity(int groupPosition);
    void onBackPressed();
}
