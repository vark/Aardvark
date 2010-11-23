/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gw.vark.editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class TargetPopup extends JPopupMenu {

  private Object[] _validTargets;
  private VEdit _editor;
  private boolean _updatingUI;


  public TargetPopup(VEdit editor, String defaultTarget, Object[] validTargets) {
    super("Enter Target:");
    _validTargets = validTargets;
    Arrays.sort(_validTargets);
    _editor = editor;

    JPanel content = new JPanel();
    this.setLayout(new BorderLayout());
    this.add(content, BorderLayout.CENTER);
    content.setLayout(new BorderLayout());
    content.add(new JLabel("Enter Target:"), BorderLayout.NORTH);

    final JList targets = new JList(_validTargets);
    targets.setFont(editor.getEditor().getEditor().getFont());
    content.add(new JScrollPane(targets), BorderLayout.SOUTH);

    targets.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        String value = targets.getSelectedValue().toString();
        _editor.invokeTarget(value);
        ((JPopupMenu) TargetPopup.this).setVisible(false);        
      }
    });
    final JTextField targetField = new JTextField(50);
    targetField.setText(defaultTarget);
    targetField.getCaret().setDot(defaultTarget.length());
    targetField.getCaret().moveDot(0);
    targetField.setFont(editor.getEditor().getEditor().getFont());
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        targetField.requestFocusInWindow();
      }
    });
    targetField.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        if (!_updatingUI) {
          _updatingUI = true;
          updateUI(true);
        }
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        if (!_updatingUI) {
          _updatingUI = true;
          updateUI(false);
        }
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        if (!_updatingUI) {
          _updatingUI = true;
          updateUI(false);
        }
      }

      private void updateUI(final boolean forwardComplete) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            final String start = targetField.getText().substring(0, targetField.getCaretPosition());
            Object[] data = filterTargets(start);
            String shortestMatch = null;
            for (Object target : data) {
              if (shortestMatch == null || target.toString().length() < shortestMatch.length()) {
                shortestMatch = target.toString();
              }
            }
            if (shortestMatch != null && forwardComplete) {
              final String finalShortestMatch = shortestMatch;
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  targetField.setText(finalShortestMatch);
                  targetField.getCaret().setDot(finalShortestMatch.length());
                  targetField.getCaret().moveDot(start.length());
                  _updatingUI = false;
                }
              });
            } else {
              _updatingUI = false;
            }
            targets.setListData(data);
          }
        });
      }
    });
    targetField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          setVisible(false);
          _editor.invokeTarget(targetField.getText());
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          int index = targets.getSelectedIndex() + 1;
          if (index < targets.getModel().getSize()) {
            targets.setSelectedIndex(index);
            targets.scrollRectToVisible(targets.getCellBounds(index, index));
          }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
          int index = targets.getSelectedIndex();
          if (index == -1) {
            index = targets.getModel().getSize();
          }
          index--;
          if (index >= 0) {
            targets.setSelectedIndex(index);
            targets.scrollRectToVisible(targets.getCellBounds(index, index));
          }
        }
      }
    });
    content.add(targetField, BorderLayout.CENTER);

    targets.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        _updatingUI = true;
        String value = targets.getSelectedValue().toString();
        targetField.setText(value);
        targetField.getCaret().setDot(value.length());
        targetField.getCaret().moveDot(0);
        _updatingUI = false;
      }
    });    

    this.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {}
      @Override
      public void focusLost(FocusEvent e) {
        ((JPopupMenu) TargetPopup.this).setVisible(false);
      }
    });
    this.pack();
  }

  private Object[] filterTargets(String start) {
    ArrayList matches = new ArrayList();
    for (Object validTarget : _validTargets) {
      if (validTarget.toString().startsWith(start)) {
        matches.add(validTarget);
      }
    }
    return matches.toArray();
  }
}
