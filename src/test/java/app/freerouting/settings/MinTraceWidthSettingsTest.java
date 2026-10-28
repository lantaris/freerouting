package app.freerouting.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.freerouting.board.actions.ItemIdGenerator;
import app.freerouting.board.model.structure.Unit;
import app.freerouting.board.state.BoardObserverAdaptor;
import app.freerouting.board.state.Communication;
import app.freerouting.io.CoordinateTransform;
import app.freerouting.settings.sources.CliSettings;
import org.junit.jupiter.api.Test;

/** CLI plumbing, helper semantics and unit conversion for the min_trace_width_um setting. */
class MinTraceWidthSettingsTest {

  private static Communication communication(Unit unit, int resolution) {
    return new Communication(
        unit,
        resolution,
        new Communication.SpecctraParserInfo("\"", null, null, null, null, false),
        new CoordinateTransform(1, 0, 0),
        new ItemIdGenerator(),
        new BoardObserverAdaptor());
  }

  @Test
  void cliFlagReachesRouterSettings() {
    CliSettings cli = new CliSettings(new String[] {"--router.min_trace_width_um=127"});
    assertEquals(127.0, cli.getSettings().getMinTraceWidthUm(), 1e-9);
  }

  @Test
  void defaultKeepsUpstreamBehaviour() {
    assertEquals(0.0, new RouterSettings().getMinTraceWidthUm(), 1e-9);
  }

  @Test
  void cloneCarriesTheField() {
    RouterSettings settings = new RouterSettings();
    settings.minTraceWidthUm = 127.0;
    assertEquals(127.0, settings.clone().getMinTraceWidthUm(), 1e-9);
  }

  @Test
  void micrometreBoardResolvesToTenUnitsPerMicrometre() {
    RouterSettings settings = new RouterSettings();
    settings.minTraceWidthUm = 127.0;
    assertEquals(
        1270, RouterSettings.minTraceWidthInBoardUnits(settings, communication(Unit.UM, 10)));
  }

  @Test
  void millimetreBoardResolvesToTheSamePhysicalWidth() {
    RouterSettings settings = new RouterSettings();
    settings.minTraceWidthUm = 127.0;
    // millimetre boards report 10000 units per millimetre, i.e. 10 units per micrometre as well
    assertEquals(
        1270, RouterSettings.minTraceWidthInBoardUnits(settings, communication(Unit.MM, 10000)));
  }

  @Test
  void milBoardResolvesToTheSamePhysicalWidth() {
    RouterSettings settings = new RouterSettings();
    settings.minTraceWidthUm = 127.0;
    // 127 um is 5 mil; a mil board with 1000 units per mil gives 5000 units
    assertEquals(
        5000, RouterSettings.minTraceWidthInBoardUnits(settings, communication(Unit.MIL, 1000)));
  }

  @Test
  void unlimitedAndUnknownInputsResolveToZero() {
    RouterSettings off = new RouterSettings();
    assertEquals(0, RouterSettings.minTraceWidthInBoardUnits(off, communication(Unit.UM, 10)));
    RouterSettings on = new RouterSettings();
    on.minTraceWidthUm = 127.0;
    assertEquals(0, RouterSettings.minTraceWidthInBoardUnits(null, communication(Unit.UM, 10)));
    assertEquals(0, RouterSettings.minTraceWidthInBoardUnits(on, null));
    on.minTraceWidthUm = -127.0;
    assertEquals(0, RouterSettings.minTraceWidthInBoardUnits(on, communication(Unit.UM, 10)));
  }

  @Test
  void boundaryValuesKeepTheExactMinimumWidthLegal() {
    RouterSettings settings = new RouterSettings();
    Communication um = communication(Unit.UM, 10);
    settings.minTraceWidthUm = 126.0;
    assertEquals(1260, RouterSettings.minTraceWidthInBoardUnits(settings, um));
    settings.minTraceWidthUm = 127.0;
    assertEquals(1270, RouterSettings.minTraceWidthInBoardUnits(settings, um));
    settings.minTraceWidthUm = 128.0;
    assertEquals(1280, RouterSettings.minTraceWidthInBoardUnits(settings, um));
    settings.minTraceWidthUm = 112.5;
    assertTrue(RouterSettings.minTraceWidthInBoardUnits(settings, um) > 0);
  }
}
