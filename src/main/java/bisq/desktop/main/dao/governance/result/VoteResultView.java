/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.main.dao.governance.result;

import bisq.desktop.common.view.ActivatableView;
import bisq.desktop.common.view.FxmlView;
import bisq.desktop.components.AutoTooltipLabel;
import bisq.desktop.components.AutoTooltipTableColumn;
import bisq.desktop.components.HyperlinkWithIcon;
import bisq.desktop.components.TableGroupHeadline;
import bisq.desktop.main.dao.governance.PhasesView;
import bisq.desktop.main.dao.governance.ProposalDisplay;
import bisq.desktop.util.GUIUtil;
import bisq.desktop.util.Layout;

import bisq.core.btc.wallet.BsqWalletService;
import bisq.core.dao.DaoFacade;
import bisq.core.dao.state.BsqStateListener;
import bisq.core.dao.state.BsqStateService;
import bisq.core.dao.state.blockchain.Block;
import bisq.core.dao.state.period.CycleService;
import bisq.core.dao.voting.ballot.Ballot;
import bisq.core.dao.voting.proposal.Proposal;
import bisq.core.dao.voting.proposal.ProposalService;
import bisq.core.dao.voting.proposal.storage.appendonly.ProposalPayload;
import bisq.core.dao.voting.voteresult.DecryptedVote;
import bisq.core.dao.voting.voteresult.EvaluatedProposal;
import bisq.core.dao.voting.voteresult.VoteResultService;
import bisq.core.locale.Res;
import bisq.core.user.Preferences;
import bisq.core.util.BsqFormatter;

import bisq.common.util.Tuple2;

import javax.inject.Inject;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javafx.geometry.Insets;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import javafx.util.Callback;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@FxmlView
public class VoteResultView extends ActivatableView<GridPane, Void> implements BsqStateListener {
    private final DaoFacade daoFacade;
    private final PhasesView phasesView;
    private final BsqStateService bsqStateService;
    private final CycleService cycleService;
    private final VoteResultService voteResultService;
    private final ProposalService proposalService;
    private final BsqWalletService bsqWalletService;
    private final Preferences preferences;
    private final BsqFormatter bsqFormatter;


    private int gridRow = 0;

    private TableView<CycleListItem> cyclesTableView;
    private final ObservableList<CycleListItem> cycleListItemList = FXCollections.observableArrayList();
    private final SortedList<CycleListItem> sortedCycleListItemList = new SortedList<>(cycleListItemList);

    private TableView<ProposalListItem> proposalsTableView;
    private final ObservableList<ProposalListItem> proposalList = FXCollections.observableArrayList();
    private final SortedList<ProposalListItem> sortedProposalList = new SortedList<>(proposalList);

    private final ObservableList<VoteListItem> voteListItemList = FXCollections.observableArrayList();
    private final SortedList<VoteListItem> sortedVoteListItemList = new SortedList<>(voteListItemList);

    private Subscription selectedProposalSubscription;
    private ChangeListener<CycleListItem> selectedVoteResultListItemListener;
    private ResultsOfCycle resultsOfCycle;
    private ProposalListItem selectedProposalListItem;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public VoteResultView(DaoFacade daoFacade,
                          PhasesView phasesView,
                          BsqStateService bsqStateService,
                          CycleService cycleService,
                          VoteResultService voteResultService,
                          ProposalService proposalService,
                          BsqWalletService bsqWalletService,
                          Preferences preferences,
                          BsqFormatter bsqFormatter) {
        this.daoFacade = daoFacade;
        this.phasesView = phasesView;
        this.bsqStateService = bsqStateService;
        this.cycleService = cycleService;
        this.voteResultService = voteResultService;
        this.proposalService = proposalService;
        this.bsqWalletService = bsqWalletService;
        this.preferences = preferences;
        this.bsqFormatter = bsqFormatter;
    }

    @Override
    public void initialize() {
        gridRow = phasesView.addGroup(root, gridRow);
        selectedVoteResultListItemListener = (observable, oldValue, newValue) -> onResultsListItemSelected(newValue);

        createCyclesTable();
    }


    @Override
    protected void activate() {
        super.activate();

        phasesView.activate();

        daoFacade.addBsqStateListener(this);
        cyclesTableView.getSelectionModel().selectedItemProperty().addListener(selectedVoteResultListItemListener);

        fillCycleList();
    }

    @Override
    protected void deactivate() {
        super.deactivate();

        onResultsListItemSelected(null);

        phasesView.deactivate();

        daoFacade.removeBsqStateListener(this);
        cyclesTableView.getSelectionModel().selectedItemProperty().removeListener(selectedVoteResultListItemListener);

        if (selectedProposalSubscription != null)
            selectedProposalSubscription.unsubscribe();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // BsqStateListener
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onNewBlockHeight(int height) {
        fillCycleList();
    }

    @Override
    public void onParseTxsComplete(Block block) {
    }

    @Override
    public void onParseBlockChainComplete() {
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI handlers
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void onResultsListItemSelected(CycleListItem item) {
        if (selectedProposalSubscription != null)
            selectedProposalSubscription.unsubscribe();

        GUIUtil.removeChildrenFromGridPaneRows(root, 2, gridRow);
        gridRow = 1;

        if (item != null) {
            resultsOfCycle = item.getResultsOfCycle();

            createProposalsTable();

            selectedProposalSubscription = EasyBind.subscribe(proposalsTableView.getSelectionModel().selectedItemProperty(),
                    this::onSelectProposalResultListItem);
        }
    }

    private void onSelectProposalResultListItem(ProposalListItem item) {
        selectedProposalListItem = item;

        GUIUtil.removeChildrenFromGridPaneRows(root, 3, gridRow);
        gridRow = 2;


        if (selectedProposalListItem != null) {

            EvaluatedProposal evaluatedProposal = selectedProposalListItem.getEvaluatedProposal();
            Optional<Ballot> optionalBallot = daoFacade.getAllBallots().stream()
                    .filter(ballot -> ballot.getProposalTxId().equals(evaluatedProposal.getProposalTxId()))
                    .findAny();
            Ballot ballot = optionalBallot.orElse(null);
            createProposalDisplay(evaluatedProposal, ballot);
            createVotesTable();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Fill lists: Cycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void fillCycleList() {
        cycleListItemList.clear();
        bsqStateService.getCycles().forEach(cycle -> {
            List<Proposal> proposalsForCycle = proposalService.getAppendOnlyStoreList().stream()
                    .filter(proposalPayload -> cycleService.isTxInCycle(cycle, proposalPayload.getProposal().getTxId()))
                    .map(ProposalPayload::getProposal)
                    .collect(Collectors.toList());

            List<EvaluatedProposal> evaluatedProposalsForCycle = voteResultService.getAllEvaluatedProposals().stream()
                    .filter(evaluatedProposal -> cycleService.isTxInCycle(cycle, evaluatedProposal.getProposal().getTxId()))
                    .collect(Collectors.toList());

            List<DecryptedVote> decryptedVotesForCycle = voteResultService.getAllDecryptedVotes().stream()
                    .filter(decryptedVote -> cycleService.isTxInCycle(cycle, decryptedVote.getBlindVoteTxId()))
                    .filter(decryptedVote -> cycleService.isTxInCycle(cycle, decryptedVote.getVoteRevealTxId()))
                    .collect(Collectors.toList());

            long cycleStartTime = bsqStateService.getBlockAtHeight(cycle.getHeightOfFirstBlock())
                    .map(e -> e.getTime() * 1000)
                    .orElse(0L);
            int cycleIndex = cycleService.getCycleIndex(cycle);
            ResultsOfCycle resultsOfCycle = new ResultsOfCycle(cycle,
                    cycleIndex,
                    cycleStartTime,
                    proposalsForCycle,
                    evaluatedProposalsForCycle,
                    decryptedVotesForCycle);
            CycleListItem cycleListItem = new CycleListItem(resultsOfCycle, bsqStateService, bsqFormatter);
            cycleListItemList.add(cycleListItem);
        });
        Collections.reverse(cycleListItemList);

        GUIUtil.setFitToRowsForTableView(cyclesTableView, 24, 28, 80);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Create views: cyclesTableView
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createCyclesTable() {
        TableGroupHeadline headline = new TableGroupHeadline(Res.get("dao.results.cycles.header"));
        GridPane.setRowIndex(headline, ++gridRow);
        GridPane.setMargin(headline, new Insets(Layout.GROUP_DISTANCE, -10, -10, -10));
        GridPane.setColumnSpan(headline, 2);
        root.getChildren().add(headline);

        cyclesTableView = new TableView<>();
        cyclesTableView.setPlaceholder(new AutoTooltipLabel(Res.get("table.placeholder.noData")));
        cyclesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        createCycleColumns(cyclesTableView);

        GridPane.setRowIndex(cyclesTableView, gridRow);
        GridPane.setMargin(cyclesTableView, new Insets(Layout.FIRST_ROW_AND_GROUP_DISTANCE, -10, -15, -10));
        GridPane.setColumnSpan(cyclesTableView, 2);
        root.getChildren().add(cyclesTableView);

        cyclesTableView.setItems(sortedCycleListItemList);
        sortedCycleListItemList.comparatorProperty().bind(cyclesTableView.comparatorProperty());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Create views: proposalsTableView
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createProposalsTable() {
        TableGroupHeadline proposalsTableHeader = new TableGroupHeadline(Res.get("dao.results.proposals.header"));
        GridPane.setRowIndex(proposalsTableHeader, ++gridRow);
        GridPane.setMargin(proposalsTableHeader, new Insets(Layout.GROUP_DISTANCE, -10, -10, -10));
        GridPane.setColumnSpan(proposalsTableHeader, 2);
        root.getChildren().add(proposalsTableHeader);

        proposalsTableView = new TableView<>();
        proposalsTableView.setPlaceholder(new AutoTooltipLabel(Res.get("table.placeholder.noData")));
        proposalsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        createProposalsColumns(proposalsTableView);

        GridPane.setRowIndex(proposalsTableView, gridRow);
        GridPane.setMargin(proposalsTableView, new Insets(Layout.FIRST_ROW_AND_GROUP_DISTANCE, -10, 5, -10));
        GridPane.setColumnSpan(proposalsTableView, 2);
        root.getChildren().add(proposalsTableView);

        proposalsTableView.setItems(sortedProposalList);
        sortedProposalList.comparatorProperty().bind(proposalsTableView.comparatorProperty());

        proposalList.clear();
        proposalList.forEach(ProposalListItem::resetTableRow);
        proposalList.setAll(resultsOfCycle.getEvaluatedProposals().stream()
                .map(evaluatedProposal -> new ProposalListItem(evaluatedProposal, bsqFormatter))
                .collect(Collectors.toList()));
        proposalList.sort(Comparator.comparing(proposalListItem -> proposalListItem.getEvaluatedProposal().getProposal().getCreationDate()));
        GUIUtil.setFitToRowsForTableView(proposalsTableView, 33, 28, 80);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Create views: proposalDisplay
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createProposalDisplay(EvaluatedProposal evaluatedProposal, Ballot ballot) {
        Proposal proposal = evaluatedProposal.getProposal();
        ProposalDisplay proposalDisplay = new ProposalDisplay(new GridPane(), bsqFormatter, bsqWalletService, daoFacade);

        ScrollPane proposalDisplayView = proposalDisplay.getView();
        GridPane.setMargin(proposalDisplayView, new Insets(0, -10, -15, -10));
        GridPane.setRowIndex(proposalDisplayView, ++gridRow);
        GridPane.setColumnSpan(proposalDisplayView, 2);
        GridPane.setHgrow(proposalDisplayView, Priority.ALWAYS);
        root.getChildren().add(proposalDisplayView);

        proposalDisplay.createAllFields(Res.get("dao.proposal.selectedProposal"), 0, 0,
                proposal.getType(), false);
        proposalDisplay.setEditable(false);

        proposalDisplay.applyProposalPayload(proposal);

        proposalDisplay.applyEvaluatedProposal(evaluatedProposal);

        Tuple2<Long, Long> meritAndStakeTuple = daoFacade.getMeritAndStakeForProposal(proposal.getTxId());
        long merit = meritAndStakeTuple.first;
        long stake = meritAndStakeTuple.second;
        proposalDisplay.applyBallotAndVoteWeight(ballot, merit, stake);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Create views: votesTableView
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createVotesTable() {
        TableGroupHeadline votesTableHeader = new TableGroupHeadline(Res.get("dao.results.proposals.voting.detail.header"));
        GridPane.setRowIndex(votesTableHeader, ++gridRow);
        GridPane.setMargin(votesTableHeader, new Insets(Layout.GROUP_DISTANCE, -10, -10, -10));
        GridPane.setColumnSpan(votesTableHeader, 2);
        root.getChildren().add(votesTableHeader);

        TableView<VoteListItem> votesTableView = new TableView<>();
        votesTableView.setPlaceholder(new AutoTooltipLabel(Res.get("table.placeholder.noData")));
        votesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        createColumns(votesTableView);
        GridPane.setRowIndex(votesTableView, gridRow);
        GridPane.setMargin(votesTableView, new Insets(Layout.FIRST_ROW_AND_GROUP_DISTANCE, -10, -15, -10));
        GridPane.setColumnSpan(votesTableView, 2);
        root.getChildren().add(votesTableView);

        votesTableView.setItems(sortedVoteListItemList);
        sortedVoteListItemList.comparatorProperty().bind(votesTableView.comparatorProperty());

        voteListItemList.clear();
        resultsOfCycle.getEvaluatedProposals().stream()
                .filter(evaluatedProposal -> evaluatedProposal.getProposal().equals(selectedProposalListItem.getEvaluatedProposal().getProposal()))
                .forEach(evaluatedProposal -> {
                    resultsOfCycle.getDecryptedVotesForCycle().forEach(decryptedVote -> {
                        voteListItemList.add(new VoteListItem(evaluatedProposal.getProposal(), decryptedVote,
                                bsqStateService, bsqFormatter));
                    });
                });

        voteListItemList.sort(Comparator.comparing(VoteListItem::getBlindVoteTxId));
        GUIUtil.setFitToRowsForTableView(votesTableView, 33, 28, 80);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // TableColumns: CycleListItem
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createCycleColumns(TableView<CycleListItem> votesTableView) {
        TableColumn<CycleListItem, CycleListItem> column;
        column = new AutoTooltipTableColumn<>(Res.get("dao.results.cycles.table.header.cycle"));
        column.setMinWidth(160);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<CycleListItem, CycleListItem>, TableCell<CycleListItem,
                        CycleListItem>>() {
                    @Override
                    public TableCell<CycleListItem, CycleListItem> call(
                            TableColumn<CycleListItem, CycleListItem> column) {
                        return new TableCell<CycleListItem, CycleListItem>() {
                            @Override
                            public void updateItem(final CycleListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getCycle());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(CycleListItem::getCycleStartTime));
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.cycles.table.header.numProposals"));
        column.setMinWidth(90);
        column.setMaxWidth(90);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<CycleListItem, CycleListItem>, TableCell<CycleListItem,
                        CycleListItem>>() {
                    @Override
                    public TableCell<CycleListItem, CycleListItem> call(
                            TableColumn<CycleListItem, CycleListItem> column) {
                        return new TableCell<CycleListItem, CycleListItem>() {
                            @Override
                            public void updateItem(final CycleListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getNumProposals());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(CycleListItem::getNumProposals));
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.cycles.table.header.numVotes"));
        column.setMinWidth(70);
        column.setMaxWidth(70);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<CycleListItem, CycleListItem>, TableCell<CycleListItem,
                        CycleListItem>>() {
                    @Override
                    public TableCell<CycleListItem, CycleListItem> call(
                            TableColumn<CycleListItem, CycleListItem> column) {
                        return new TableCell<CycleListItem, CycleListItem>() {
                            @Override
                            public void updateItem(final CycleListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getNumVotesAsString());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(CycleListItem::getNumProposals));
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.cycles.table.header.voteWeight"));
        column.setMinWidth(70);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<CycleListItem, CycleListItem>, TableCell<CycleListItem,
                        CycleListItem>>() {
                    @Override
                    public TableCell<CycleListItem, CycleListItem> call(
                            TableColumn<CycleListItem, CycleListItem> column) {
                        return new TableCell<CycleListItem, CycleListItem>() {
                            @Override
                            public void updateItem(final CycleListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getMeritAndStake());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(CycleListItem::getNumProposals));
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.cycles.table.header.issuance"));
        column.setMinWidth(70);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<CycleListItem, CycleListItem>, TableCell<CycleListItem,
                        CycleListItem>>() {
                    @Override
                    public TableCell<CycleListItem, CycleListItem> call(
                            TableColumn<CycleListItem, CycleListItem> column) {
                        return new TableCell<CycleListItem, CycleListItem>() {
                            @Override
                            public void updateItem(final CycleListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getIssuance());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(CycleListItem::getNumProposals));
        votesTableView.getColumns().add(column);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // TableColumns: ProposalListItem
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createProposalsColumns(TableView<ProposalListItem> votesTableView) {
        TableColumn<ProposalListItem, ProposalListItem> column;

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.proposals.table.header.proposalOwnerName"));
        column.setMinWidth(110);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<ProposalListItem, ProposalListItem>, TableCell<ProposalListItem,
                        ProposalListItem>>() {
                    @Override
                    public TableCell<ProposalListItem, ProposalListItem> call(
                            TableColumn<ProposalListItem, ProposalListItem> column) {
                        return new TableCell<ProposalListItem, ProposalListItem>() {

                            @Override
                            public void updateItem(final ProposalListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    item.setTableRow(getTableRow());
                                    setText(item.getProposalOwnerName());
                                } else {
                                    setText("");
                                }
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(ProposalListItem::getProposalOwnerName));
        votesTableView.getColumns().add(column);


        column = new AutoTooltipTableColumn<>(Res.get("dao.results.proposals.table.header.details"));
        column.setMinWidth(100);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<ProposalListItem, ProposalListItem>, TableCell<ProposalListItem,
                        ProposalListItem>>() {
                    @Override
                    public TableCell<ProposalListItem, ProposalListItem> call(
                            TableColumn<ProposalListItem, ProposalListItem> column) {
                        return new TableCell<ProposalListItem, ProposalListItem>() {
                            @Override
                            public void updateItem(final ProposalListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getIssuance());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        column.setComparator(Comparator.comparing(ProposalListItem::getThreshold));
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.proposals.table.header.result"));
        column.setMinWidth(90);
        column.setMaxWidth(column.getMinWidth());
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(new Callback<TableColumn<ProposalListItem, ProposalListItem>,
                TableCell<ProposalListItem, ProposalListItem>>() {
            @Override
            public TableCell<ProposalListItem, ProposalListItem> call(TableColumn<ProposalListItem,
                    ProposalListItem> column) {
                return new TableCell<ProposalListItem, ProposalListItem>() {
                    Label icon;

                    @Override
                    public void updateItem(final ProposalListItem item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null && !empty) {
                            icon = new Label();
                            AwesomeDude.setIcon(icon, item.getIcon());
                            icon.getStyleClass().add(item.getColorStyleClass());
                            setGraphic(icon);
                        } else {
                            setGraphic(null);
                            if (icon != null)
                                icon = null;
                        }
                    }
                };
            }
        });
        votesTableView.getColumns().add(column);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // TableColumns: VoteListItem
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createColumns(TableView<VoteListItem> votesTableView) {
        TableColumn<VoteListItem, VoteListItem> column;

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.votes.table.header.vote"));
        column.setSortable(false);
        column.setMinWidth(50);
        column.setMaxWidth(column.getMinWidth());
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<VoteListItem, VoteListItem>, TableCell<VoteListItem,
                        VoteListItem>>() {
                    @Override
                    public TableCell<VoteListItem, VoteListItem> call(
                            TableColumn<VoteListItem, VoteListItem> column) {
                        return new TableCell<VoteListItem, VoteListItem>() {
                            private Label icon;

                            @Override
                            public void updateItem(final VoteListItem item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    Tuple2<AwesomeIcon, String> iconStyleTuple = item.getIconStyleTuple();
                                    icon = new Label();
                                    AwesomeDude.setIcon(icon, iconStyleTuple.first);
                                    icon.getStyleClass().add(iconStyleTuple.second);
                                    setGraphic(icon);
                                } else {
                                    setGraphic(null);
                                }
                            }
                        };
                    }
                });
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.votes.table.header.stakeAndMerit"));
        column.setSortable(false);
        column.setMinWidth(100);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<VoteListItem, VoteListItem>, TableCell<VoteListItem,
                        VoteListItem>>() {
                    @Override
                    public TableCell<VoteListItem, VoteListItem> call(
                            TableColumn<VoteListItem, VoteListItem> column) {
                        return new TableCell<VoteListItem, VoteListItem>() {
                            @Override
                            public void updateItem(final VoteListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getMeritAndStake());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        votesTableView.getColumns().add(column);
        column = new AutoTooltipTableColumn<>(Res.get("dao.results.votes.table.header.merit"));
        column.setSortable(false);
        column.setMinWidth(100);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<VoteListItem, VoteListItem>, TableCell<VoteListItem,
                        VoteListItem>>() {
                    @Override
                    public TableCell<VoteListItem, VoteListItem> call(
                            TableColumn<VoteListItem, VoteListItem> column) {
                        return new TableCell<VoteListItem, VoteListItem>() {
                            @Override
                            public void updateItem(final VoteListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getMerit());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.votes.table.header.stake"));
        column.setSortable(false);
        column.setMinWidth(100);
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(
                new Callback<TableColumn<VoteListItem, VoteListItem>, TableCell<VoteListItem,
                        VoteListItem>>() {
                    @Override
                    public TableCell<VoteListItem, VoteListItem> call(
                            TableColumn<VoteListItem, VoteListItem> column) {
                        return new TableCell<VoteListItem, VoteListItem>() {
                            @Override
                            public void updateItem(final VoteListItem item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.getStake());
                                else
                                    setText("");
                            }
                        };
                    }
                });
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.votes.table.header.blindVoteTxId"));
        column.setSortable(false);
        column.setMinWidth(130);
        column.setMaxWidth(column.getMinWidth());
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(new Callback<TableColumn<VoteListItem, VoteListItem>,
                TableCell<VoteListItem, VoteListItem>>() {
            @Override
            public TableCell<VoteListItem, VoteListItem> call(TableColumn<VoteListItem,
                    VoteListItem> column) {
                return new TableCell<VoteListItem, VoteListItem>() {
                    private HyperlinkWithIcon hyperlinkWithIcon;

                    @Override
                    public void updateItem(final VoteListItem item, boolean empty) {
                        super.updateItem(item, empty);

                        //noinspection Duplicates
                        if (item != null && !empty) {
                            String blindVoteTxId = item.getBlindVoteTxId();
                            hyperlinkWithIcon = new HyperlinkWithIcon(blindVoteTxId, AwesomeIcon.EXTERNAL_LINK);
                            hyperlinkWithIcon.setOnAction(event -> openTxInBlockExplorer(item.getBlindVoteTxId()));
                            hyperlinkWithIcon.setTooltip(new Tooltip(Res.get("tooltip.openBlockchainForTx", blindVoteTxId)));
                            setGraphic(hyperlinkWithIcon);
                        } else {
                            setGraphic(null);
                            if (hyperlinkWithIcon != null)
                                hyperlinkWithIcon.setOnAction(null);
                        }
                    }
                };
            }
        });
        votesTableView.getColumns().add(column);

        column = new AutoTooltipTableColumn<>(Res.get("dao.results.votes.table.header.voteRevealTxId"));
        column.setSortable(false);
        column.setMinWidth(140);
        column.setMaxWidth(column.getMinWidth());
        column.setCellValueFactory((item) -> new ReadOnlyObjectWrapper<>(item.getValue()));
        column.setCellFactory(new Callback<TableColumn<VoteListItem, VoteListItem>,
                TableCell<VoteListItem, VoteListItem>>() {
            @Override
            public TableCell<VoteListItem, VoteListItem> call(TableColumn<VoteListItem,
                    VoteListItem> column) {
                return new TableCell<VoteListItem, VoteListItem>() {
                    private HyperlinkWithIcon hyperlinkWithIcon;

                    @Override
                    public void updateItem(final VoteListItem item, boolean empty) {
                        super.updateItem(item, empty);

                        //noinspection Duplicates
                        if (item != null && !empty) {
                            String voteRevealTxId = item.getVoteRevealTxId();
                            hyperlinkWithIcon = new HyperlinkWithIcon(voteRevealTxId, AwesomeIcon.EXTERNAL_LINK);
                            hyperlinkWithIcon.setOnAction(event -> openTxInBlockExplorer(item.getVoteRevealTxId()));
                            hyperlinkWithIcon.setTooltip(new Tooltip(Res.get("tooltip.openBlockchainForTx", voteRevealTxId)));
                            setGraphic(hyperlinkWithIcon);
                        } else {
                            setGraphic(null);
                            if (hyperlinkWithIcon != null)
                                hyperlinkWithIcon.setOnAction(null);
                        }
                    }
                };
            }
        });
        votesTableView.getColumns().add(column);
    }

    private void openTxInBlockExplorer(String txId) {
        if (txId != null)
            GUIUtil.openWebPage(preferences.getBsqBlockChainExplorer().txUrl + txId);
    }
}
