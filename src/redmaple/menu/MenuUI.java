package redmaple.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.tablelayout.Value;
import redmaple.RedMaple;
import redmaple.menu.ui.FilteredList;
import redmaple.menu.ui.TextureDrawable;
import redmaple.songfinder.Songfinder;
import redmaple.songfinder.StoredMusic;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 22:50
 * To change this template use File | Settings | File Templates.
 */
public class MenuUI {
    MenuScreen screen;
    Table table, buttonsTable, subMenuTable;
    public Skin skin;

    public MenuUI(MenuScreen screen) {
        this.screen = screen;
    }

    public void create() {
        table = new Table();
        table.setFillParent(true);
        table.debug();

        table.setBackground(new TextureDrawable(screen.background));

        buttonsTable = new Table();
        buttonsTable.debug();

        subMenuTable = new Table();
        subMenuTable.debug();

        table.add(buttonsTable).expand().fill();
        table.add(subMenuTable);

        screen.stage.addActor(table);

        skin = new Skin(Gdx.files.internal("ui/springui.json"), new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas")));

        // We can do this DRY-violating block of code for every button because we got so few of them
        {
            TextButton btn = new TextButton("Start game", skin);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    setSubMenuState(SubMenu.MusicList);

                }
            });
            buttonsTable.add(btn)
                    .padBottom(1)
                    .expand()
                    .prefWidth(Value.percentWidth(0.8f))
                    .prefHeight(90);
            buttonsTable.row();


        }

        screen.stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.BACK) {
                    Gdx.app.log("RedMaple", "Back key was pressed!");
                    if (setSubMenuState(null)) {
                        return true;
                    }
                    Gdx.app.log("RedMaple", "We're in main menu. Closing..");
                    // SubMenu was already null
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });

        {

            Texture fbIcon = new Texture(Gdx.files.internal("gfx/soc/facebook.png"));

            ImageButton imageButton = new ImageButton(new TextureDrawable(fbIcon));

            buttonsTable.add(imageButton)
                    .expand()
                    .bottom();
            buttonsTable.row();
        }

    }

    public void draw() {
        /*screen.batch.begin();

        screen.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        screen.batch.end();
          */
        //Table.drawDebug(screen.stage);
    }

    private void createMusicList() {
        final Songfinder songfinder = RedMaple.songfinder;
        FileHandle handle = songfinder.openNativeFileSelector();
        if (handle != null)
            return;

        StoredMusic[] music = songfinder.getStoredMusic();

        Table t = new Table();
        t.setFillParent(true);
        t.debug();

        t.top();

        final Table selectedControls = new Table();
        selectedControls.setVisible(true);

        final TextField searchField = new TextField("", skin);

        final FilteredList musicList = new FilteredList(music, skin);
        final FilteredList.ListFilter filter = new FilteredList.ListFilter(null) {
            @Override
            public boolean accept(Object o, String argument) {
                final String str1 = ((StoredMusic)o).cachedLowerToString;
                boolean res = str1.contains(argument);
                return res;
            }
        };

        searchField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char key) {
                //Gdx.app.log("RedMaple", "Pressed key " + ((int)key) + " (\\n == " + ((int)'\n') + ")");
                if (key == '\n' || key == 13 /* desktop enter */) {
                    textField.getOnscreenKeyboard().show(false);
                    Gdx.app.log("RedMaple", "Searching for " + textField.getText());
                    filter.argument = textField.getText().toLowerCase();
                    if (textField.getText().isEmpty())
                        musicList.updateFilter(null);
                    else
                        musicList.updateFilter(filter);
                }
            }

        });

        {
            TextButton btn = new TextButton("Play", skin);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    screen.redMaple.gameScreen.setMusic((StoredMusic) musicList.getSelectedObject(), false);
                    screen.stage.addAction(Actions.sequence(
                            Actions.alpha(0, 0.4f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    screen.redMaple.switchTo(RedMaple.GScreen.Game);
                                }
                            }),
                            Actions.alpha(1) // need to show this after anyway
                    ));
                }
            });

            selectedControls.add(btn);
        }

        final ScrollPane sp = new ScrollPane(musicList, skin);

        {
            TextButton btn = new TextButton("Random", skin);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int idx = MathUtils.random(musicList.getObjectsLength() - 1);
                    musicList.setSelectedIndex(idx);
                    sp.setScrollY(musicList.getHeight()/musicList.getObjectsLength()*idx - 200);
                }
            });

            selectedControls.add(btn);
        }
        {
            TextButton btn = new TextButton("Regenerate", skin);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    screen.redMaple.gameScreen.setMusic((StoredMusic) musicList.getSelectedObject(), true);
                    screen.stage.addAction(Actions.sequence(
                            Actions.alpha(0, 0.4f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    screen.redMaple.switchTo(RedMaple.GScreen.Game);
                                }
                            })
                    ));
                }
            });

            selectedControls.add(btn);
        }

        t.add(searchField)
                .minWidth(Value.percentWidth(0.5f).width(table))
                .expandX()
                .top();
        t.row();

        musicList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                StoredMusic sm = (StoredMusic) musicList.getSelectedObject();
                if (sm == null) {
                    selectedControls.setVisible(false);
                } else {
                    selectedControls.setVisible(true);
                }
                subMenuTable.invalidateHierarchy();
            }
        });

        t.add(sp)
                .fill()
                .maxWidth(Value.percentWidth(0.5f).width(table))
                .expand();

        t.row();

        t.add(selectedControls).expandX().fill();

        subMenuTable.add(t);
    }

    SubMenu csm;
    public boolean setSubMenuState(SubMenu sm) {
        if (csm == sm) return false;
        csm = sm;

        subMenuTable.clear();
        if (csm == null) return true; // It's null so we kinda succeeded

        switch (csm) {
            case MusicList:


                createMusicList();

                break;
        }

        return true;
    }

    public enum SubMenu {
        MusicList
    }

}
