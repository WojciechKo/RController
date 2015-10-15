// Generated code from Butter Knife. Do not modify!
package info.korzeniowski.rcontroller;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends info.korzeniowski.rcontroller.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492966, "field 'toolbar'");
    target.toolbar = finder.castView(view, 2131492966, "field 'toolbar'");
    view = finder.findRequiredView(source, 2131492967, "field 'fab'");
    target.fab = finder.castView(view, 2131492967, "field 'fab'");
    view = finder.findRequiredView(source, 2131492968, "field 'text'");
    target.text = finder.castView(view, 2131492968, "field 'text'");
  }

  @Override public void unbind(T target) {
    target.toolbar = null;
    target.fab = null;
    target.text = null;
  }
}
