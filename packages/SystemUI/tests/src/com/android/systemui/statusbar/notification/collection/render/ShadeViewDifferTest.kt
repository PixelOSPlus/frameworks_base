/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.statusbar.notification.collection.render

import android.content.Context
import android.testing.AndroidTestingRunner
import android.view.View
import android.widget.FrameLayout
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.util.mockito.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidTestingRunner::class)
class ShadeViewDifferTest : SysuiTestCase() {
    private lateinit var differ: ShadeViewDiffer
    private val rootController = FakeController(mContext, "RootController")
    private val controller1 = FakeController(mContext, "Controller1")
    private val controller2 = FakeController(mContext, "Controller2")
    private val controller3 = FakeController(mContext, "Controller3")
    private val controller4 = FakeController(mContext, "Controller4")
    private val controller5 = FakeController(mContext, "Controller5")
    private val controller6 = FakeController(mContext, "Controller6")
    private val controller7 = FakeController(mContext, "Controller7")
    private val logger: ShadeViewDifferLogger = mock()

    @Before
    fun setUp() {
        differ = ShadeViewDiffer(rootController, logger)
    }

    @Test
    fun testAddInitialViews() {
        // WHEN a spec is applied to an empty root
        // THEN the final tree matches the spec
        applySpecAndCheck(
            node(controller1),
            node(controller2, node(controller3), node(controller4)),
            node(controller5)
        )
    }

    @Test
    fun testDetachViews() {
        // GIVEN a preexisting tree of controllers
        applySpecAndCheck(
            node(controller1),
            node(controller2, node(controller3), node(controller4)),
            node(controller5)
        )

        // WHEN the new spec removes nodes
        // THEN the final tree matches the spec
        applySpecAndCheck(node(controller5))
    }

    @Test
    fun testReparentChildren() {
        // GIVEN a preexisting tree of controllers
        applySpecAndCheck(
            node(controller1),
            node(controller2, node(controller3), node(controller4)),
            node(controller5)
        )

        // WHEN the parents of the controllers are all shuffled around
        // THEN the final tree matches the spec
        applySpecAndCheck(
            node(controller1),
            node(controller4),
            node(controller3, node(controller2))
        )
    }

    @Test
    fun testReorderChildren() {
        // GIVEN a preexisting tree of controllers
        applySpecAndCheck(
            node(controller1),
            node(controller2),
            node(controller3),
            node(controller4)
        )

        // WHEN the children change order
        // THEN the final tree matches the spec
        applySpecAndCheck(
            node(controller3),
            node(controller2),
            node(controller4),
            node(controller1)
        )
    }

    @Test
    fun testRemovedGroupsAreBrokenApart() {
        // GIVEN a preexisting tree with a group
        applySpecAndCheck(
            node(controller1),
            node(controller2, node(controller3), node(controller4), node(controller5))
        )

        // WHEN the new spec removes the entire group
        applySpecAndCheck(node(controller1))

        // THEN the group children are no longer attached to their parent
        Assert.assertNull(controller3.view.parent)
        Assert.assertNull(controller4.view.parent)
        Assert.assertNull(controller5.view.parent)
    }

    @Test
    fun testUnmanagedViews() {
        // GIVEN a preexisting tree of controllers
        applySpecAndCheck(
            node(controller1),
            node(controller2, node(controller3), node(controller4)),
            node(controller5)
        )

        // GIVEN some additional unmanaged views attached to the tree
        val unmanagedView1 = View(mContext)
        val unmanagedView2 = View(mContext)
        rootController.view.addView(unmanagedView1, 1)
        controller2.view.addView(unmanagedView2, 0)

        // WHEN a new spec is applied with additional nodes
        // THEN the final tree matches the spec
        applySpecAndCheck(
            node(controller1),
            node(controller2, node(controller3), node(controller4), node(controller6)),
            node(controller5),
            node(controller7)
        )

        // THEN the unmanaged views have been pushed to the end of their parents
        Assert.assertEquals(unmanagedView1, rootController.view.getChildAt(4))
        Assert.assertEquals(unmanagedView2, controller2.view.getChildAt(3))
    }

    private fun applySpecAndCheck(spec: NodeSpec) {
        differ.applySpec(spec)
        checkMatchesSpec(spec)
    }

    private fun applySpecAndCheck(vararg children: SpecBuilder) {
        applySpecAndCheck(node(rootController, *children).build())
    }

    private fun checkMatchesSpec(spec: NodeSpec) {
        val parent = spec.controller
        val children = spec.children
        for (i in children.indices) {
            val childSpec = children[i]
            val view = parent.getChildAt(i)
            Assert.assertEquals(
                "Child $i of parent ${parent.nodeLabel} " +
                    "should be ${childSpec.controller.nodeLabel} " +
                    "but instead " +
                    view?.let(differ::getViewLabel),
                view,
                childSpec.controller.view
            )
            if (childSpec.children.isNotEmpty()) {
                checkMatchesSpec(childSpec)
            }
        }
    }

    private class FakeController(context: Context, label: String) : NodeController {
        override val view: FrameLayout = FrameLayout(context)
        override val nodeLabel: String = label
        override fun getChildCount(): Int = view.childCount

        override fun getChildAt(index: Int): View? {
            return view.getChildAt(index)
        }

        override fun addChildAt(child: NodeController, index: Int) {
            view.addView(child.view, index)
        }

        override fun moveChildTo(child: NodeController, index: Int) {
            view.removeView(child.view)
            view.addView(child.view, index)
        }

        override fun removeChild(child: NodeController, isTransfer: Boolean) {
            view.removeView(child.view)
        }

        override fun onViewAdded() {}
        override fun onViewMoved() {}
        override fun onViewRemoved() {}
    }

    private class SpecBuilder(
        private val mController: NodeController,
        private val children: Array<out SpecBuilder>
    ) {

        @JvmOverloads
        fun build(parent: NodeSpec? = null): NodeSpec {
            val spec = NodeSpecImpl(parent, mController)
            for (childBuilder in children) {
                spec.children.add(childBuilder.build(spec))
            }
            return spec
        }
    }

    companion object {
        private fun node(controller: NodeController, vararg children: SpecBuilder): SpecBuilder {
            return SpecBuilder(controller, children)
        }
    }
}
