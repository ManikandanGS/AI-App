import React, { useState } from 'react';
import { FaStar } from 'react-icons/fa';
import toast from 'react-hot-toast';
import { submitFeedback } from '../services/api';

const FeedbackPanel = ({ resolutionId }) => {
  const [rating, setRating] = useState(0);
  const [hover, setHover] = useState(0);
  const [feedbackText, setFeedbackText] = useState('');
  const [wasFixApplied, setWasFixApplied] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      toast.error('Please provide a rating');
      return;
    }
    
    setSubmitting(true);
    try {
      await submitFeedback({
        resolutionId,
        rating,
        feedbackText,
        wasFixApplied
      });
      setSubmitted(true);
      toast.success('Thank you for your feedback!');
    } catch (error) {
      console.error(error);
      toast.error('Failed to submit feedback');
    } finally {
      setSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="bg-green-500/10 border border-green-500/20 p-6 rounded-xl text-center">
        <h3 className="text-xl font-bold text-green-400 mb-2">Feedback Received</h3>
        <p className="text-green-300/70">Thank you for helping improve the AI Bug Resolver.</p>
      </div>
    );
  }

  return (
    <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 shadow-sm">
      <h3 className="text-xl font-bold text-white mb-4">Provide Feedback</h3>
      <form onSubmit={handleSubmit} className="space-y-5">
        
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">Rate this analysis</label>
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                type="button"
                key={star}
                className="focus:outline-none transition-transform hover:scale-110"
                onClick={() => setRating(star)}
                onMouseEnter={() => setHover(star)}
                onMouseLeave={() => setHover(0)}
              >
                <FaStar
                  className={`text-3xl ${
                    star <= (hover || rating) ? 'text-yellow-400' : 'text-slate-600'
                  }`}
                />
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">Comments (Optional)</label>
          <textarea
            value={feedbackText}
            onChange={(e) => setFeedbackText(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg px-4 py-3 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 min-h-[100px]"
            placeholder="What was good or bad about this analysis?"
            disabled={submitting}
          ></textarea>
        </div>

        <label className="flex items-center gap-3 cursor-pointer">
          <input
            type="checkbox"
            checked={wasFixApplied}
            onChange={(e) => setWasFixApplied(e.target.checked)}
            className="w-5 h-5 rounded border-slate-700 bg-slate-900 text-indigo-600 focus:ring-indigo-600 focus:ring-offset-slate-900"
            disabled={submitting}
          />
          <span className="text-slate-300 font-medium">Was the suggested fix applied?</span>
        </label>

        <button
          type="submit"
          disabled={submitting}
          className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-6 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:opacity-50"
        >
          {submitting ? 'Submitting...' : 'Submit Feedback'}
        </button>
      </form>
    </div>
  );
};

export default FeedbackPanel;
