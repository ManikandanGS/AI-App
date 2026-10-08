import React, { useState, useEffect } from 'react';
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa';
import toast from 'react-hot-toast';
import { getConfigs, createConfig, updateConfig, deleteConfig } from '../services/api';
import Modal from './common/Modal';
import LoadingSpinner from './common/LoadingSpinner';

const initialFormState = {
  projectKey: '',
  jiraBaseUrl: '',
  jiraUsername: '',
  jiraApiToken: '',
  bitbucketBaseUrl: '',
  bitbucketUsername: '',
  bitbucketAppPassword: '',
  bitbucketWorkspace: '',
  bitbucketRepoSlug: '',
  dbUrl: '',
  dbUsername: '',
  dbPassword: '',
};

const ConfigPanel = () => {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState(initialFormState);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);

  const loadConfigs = async () => {
    try {
      const data = await getConfigs();
      setConfigs(data || []);
    } catch (error) {
      console.error(error);
      toast.error('Failed to load configs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadConfigs();
  }, []);

  const handleOpenModal = (config = null) => {
    if (config) {
      setFormData({ ...config, jiraApiToken: '', bitbucketAppPassword: '', dbPassword: '' });
      setEditingId(config.id);
    } else {
      setFormData(initialFormState);
      setEditingId(null);
    }
    setIsModalOpen(true);
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (editingId) {
        await updateConfig(editingId, formData);
        toast.success('Configuration updated');
      } else {
        await createConfig(formData);
        toast.success('Configuration created');
      }
      setIsModalOpen(false);
      loadConfigs();
    } catch (error) {
      console.error(error);
      toast.error(error.response?.data?.message || 'Failed to save configuration');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this configuration?')) return;
    try {
      await deleteConfig(id);
      toast.success('Configuration deleted');
      loadConfigs();
    } catch (error) {
      console.error(error);
      toast.error('Failed to delete configuration');
    }
  };

  if (loading) return <LoadingSpinner text="Loading Configurations..." />;

  const InputGroup = ({ label, name, type = "text", required = true }) => (
    <div>
      <label className="block text-sm font-medium text-slate-300 mb-1">{label}</label>
      <input
        type={type}
        name={name}
        value={formData[name] || ''}
        onChange={handleChange}
        required={required && !editingId}
        className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg px-3 py-2 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 text-sm"
      />
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Project Configurations</h2>
        <button
          onClick={() => handleOpenModal()}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg font-medium transition-colors"
        >
          <FaPlus /> Add New Config
        </button>
      </div>

      <div className="bg-slate-800 rounded-xl border border-slate-700 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-900/50 text-slate-400">
              <tr>
                <th className="px-6 py-4 font-medium">Project Key</th>
                <th className="px-6 py-4 font-medium">Jira URL</th>
                <th className="px-6 py-4 font-medium">Repo Slug</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/50">
              {configs.length === 0 ? (
                <tr><td colSpan="4" className="px-6 py-8 text-center text-slate-500">No configurations found.</td></tr>
              ) : (
                configs.map((config) => (
                  <tr key={config.id} className="hover:bg-slate-700/30 transition-colors">
                    <td className="px-6 py-4 font-bold text-white">{config.projectKey}</td>
                    <td className="px-6 py-4">{config.jiraBaseUrl}</td>
                    <td className="px-6 py-4">{config.bitbucketRepoSlug}</td>
                    <td className="px-6 py-4 flex justify-end gap-3">
                      <button onClick={() => handleOpenModal(config)} className="text-blue-400 hover:text-blue-300 p-2"><FaEdit /></button>
                      <button onClick={() => handleDelete(config.id)} className="text-red-400 hover:text-red-300 p-2"><FaTrash /></button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingId ? 'Edit Configuration' : 'New Configuration'}>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-4 border-b border-slate-700 pb-6">
            <h3 className="text-lg font-semibold text-white">Project Setup</h3>
            <InputGroup label="Project Key (e.g. PROJ)" name="projectKey" />
          </div>

          <div className="space-y-4 border-b border-slate-700 pb-6">
            <h3 className="text-lg font-semibold text-white">Jira Settings</h3>
            <InputGroup label="Jira Base URL" name="jiraBaseUrl" />
            <div className="grid grid-cols-2 gap-4">
              <InputGroup label="Username (Email)" name="jiraUsername" />
              <InputGroup label="API Token" name="jiraApiToken" type="password" required={!editingId} />
            </div>
          </div>

          <div className="space-y-4 border-b border-slate-700 pb-6">
            <h3 className="text-lg font-semibold text-white">Bitbucket Settings</h3>
            <InputGroup label="Bitbucket Base URL" name="bitbucketBaseUrl" />
            <div className="grid grid-cols-2 gap-4">
              <InputGroup label="Workspace" name="bitbucketWorkspace" />
              <InputGroup label="Repo Slug" name="bitbucketRepoSlug" />
              <InputGroup label="Username" name="bitbucketUsername" />
              <InputGroup label="App Password" name="bitbucketAppPassword" type="password" required={!editingId} />
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white">Database Settings (Optional)</h3>
            <InputGroup label="Database URL" name="dbUrl" required={false} />
            <div className="grid grid-cols-2 gap-4">
              <InputGroup label="DB Username" name="dbUsername" required={false} />
              <InputGroup label="DB Password" name="dbPassword" type="password" required={false} />
            </div>
          </div>

          <div className="pt-4 flex justify-end gap-3">
            <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 rounded-lg font-medium text-slate-300 hover:bg-slate-700 transition-colors">Cancel</button>
            <button type="submit" disabled={saving} className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg font-medium transition-colors disabled:opacity-50">
              {saving ? 'Saving...' : 'Save Configuration'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ConfigPanel;
