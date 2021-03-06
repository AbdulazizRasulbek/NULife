package com.naltynbekkz.nulife.ui.courses.questions.front

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naltynbekkz.nulife.R
import com.naltynbekkz.nulife.di.ViewModelProviderFactory
import com.naltynbekkz.nulife.model.Question
import com.naltynbekkz.nulife.model.User
import com.naltynbekkz.nulife.ui.MainActivity
import com.naltynbekkz.nulife.ui.courses.courses.front.CourseFragmentDirections
import com.naltynbekkz.nulife.ui.courses.questions.adapter.TopicsAdapter
import com.naltynbekkz.nulife.ui.courses.questions.viewmodel.QuestionsViewModel
import com.naltynbekkz.nulife.util.EditDeleteBottomSheet
import kotlinx.android.synthetic.main.fragment_questions.*
import javax.inject.Inject

class QuestionsFragment : Fragment() {

    @Inject
    lateinit var viewModelProvider: ViewModelProviderFactory
    private val viewModel: QuestionsViewModel by viewModels {
        viewModelProvider.create(this, arguments)
    }

    private var user: User? = null

    private lateinit var topicsAdapter: TopicsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).coursesComponent.inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_questions, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        topicsAdapter = TopicsAdapter(
            click = fun(question: Question) {
                findNavController().navigate(
                    CourseFragmentDirections.actionCourseFragmentToAnswersFragment(question)
                )
            },
            longClick = fun(question): Boolean {
                user?.let {
                    if (question.author.id == it.uid || question.author.id == it.anonymousId) {
                        EditDeleteBottomSheet(
                            edit = fun() {
                                findNavController().navigate(
                                    CourseFragmentDirections.actionCourseFragmentToNewQuestionFragment(
                                        question,
                                        null
                                    )
                                )
                            },
                            delete = fun() {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle("Are you sure?")
                                    .setPositiveButton("Delete") { _, _ ->
                                        viewModel.delete(question)
                                    }
                                    .setNegativeButton("Cancel") { _, _ ->

                                    }.show()
                            }
                        ).show(parentFragmentManager, "tag")
                    } else {
                        FollowBottomSheet(
                            question.id,
                            question.following,
                            viewModel::follow
                        ).show(
                            parentFragmentManager,
                            "tag"
                        )
                    }
                }
                return true
            }
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner, Observer {
            this.user = it
        })

        viewModel.questions.observe(viewLifecycleOwner, Observer {
            topicsAdapter.submitList(it)
        })

        recycler_view.adapter = topicsAdapter

    }
}
